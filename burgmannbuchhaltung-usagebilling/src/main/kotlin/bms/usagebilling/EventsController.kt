package bms.usagebilling

import bms.usagebilling.service.events.EventService
import bms.usagebilling.service.events.InsertUsageEvent
import bms.usagebilling.service.events.UsageEvent
import bms.usagebilling.web.CallAuthentication.Companion.authorizationFromCall
import bms.usagebilling.web.UnauthorizedException
import bms.usagebilling.web.illegalArgument
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

fun Application.eventsBilling() {
    routing {
        authenticate {
            route("events", {
                securitySchemeName = "jwt"
                tags = listOf("Events")
            }) {
                billingUsageRoutes()
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.queryParams() = call.request.queryParameters

private val usageEventListExample = listOf(UsageEvent.minimalExample, UsageEvent.fullExample)
private val insertUsageEventListExample = listOf(InsertUsageEvent.minimalExample, InsertUsageEvent.fullExample)
internal fun dateTimeExample() = Clock.System.now().run {
    minus(nanosecondsOfSecond.nanoseconds).plus(123.milliseconds)
}

@Serializable
@SerialName("PushResult")
data class PushResult(val eventsWritten: Long, val totalBytes: Long, val eventsOk: Int, val eventsFailed: Int)

private fun Route.billingUsageRoutes() {

    fun PipelineContext<Unit, ApplicationCall>.getProjectId(): UUID? {
        val requestedProjectIdString = queryParams()["project"]
            ?.let { runCatching { UUID(it) }.getOrElse { illegalArgument("Invalid project uuid: ${it.message}") } }

        val callAuth = authorizationFromCall()
        val projectId = when {
            callAuth.allowedProjectId == null && requestedProjectIdString == null -> null
            callAuth.allowedProjectId != null && requestedProjectIdString == null ->
                throw UnauthorizedException("Filter was not restricted to any project (all projects were requested), but API key is locked to project: ${callAuth.allowedProjectId}")

            callAuth.allowedProjectId == requestedProjectIdString -> callAuth.allowedProjectId
            callAuth.allowedProjectId == null -> requestedProjectIdString
            else -> error("Could not determine project id")
        }

        return projectId
    }

    get("query", {
        summary = "Query the organization or the project of the organization for a list of events, " +
                "optionally specify certain filters."
        description = """
            List all events of this organization that match a provided query.
            You can query by setting various filters as listed below.
            If you do not set a filter, the list will be unfiltered - thus all recorded events are returned.
            The default limit set is 1000 events per request. You can set an offset (to get the next 1000 events) with the `limit` filter.
            It is required to set the `project` filter if the API key is locked to an specific project. If the API key is not locked to a specific project, you are allowed to leave `project` empty, and thus query all events over all projects of the organization.
            """.trimIndent()
        request {
            queryParameter<String?>("project") {
                description =
                    "The `project` ID to filter by. Will list all projects if not set. If the API key is locked to a project, has to be set to that project."
                example = UUID.generateUUID().toString()
            }
            queryParameter<Array<String>?>("filterEvents") {
                description = "Event(s) to filter for."
                example =
                    arrayOf("WeatherForecast24hRequested", "WeatherForecast7dRequested", "WeatherForecast30dRequested")
            }
            queryParameter<String>("limit") {
                description =
                    "The `offset` index (number of entries to skip) and the `limit` of entries to return. Maximum of 5000 entries can be returned for a normal API session. By default: 0,1000 (skip first 0 entries; 1000 entries max)."
                example = "0,1000"
            }
            queryParameter<String>("duringTime") {
                description =
                    "Filter for events that happened during a specific time frame. Begin and end timestamps in **UTC timezone**! Timestamps may include precision up to (max. 999) milliseconds."
                //example = "2024-01-01 05:30:30.1234,2024-01-02 07:45:45.4567"
                val currentTime = dateTimeExample()
                example = "${currentTime.toLocalDateTime(TimeZone.UTC)},${
                    currentTime
                        .plus(26.hours).plus(30.minutes).plus(30.seconds).plus(333.milliseconds)
                        .toLocalDateTime(TimeZone.UTC)
                }"
            }
            queryParameter<Boolean>("orderDescending") {
                description =
                    "If the results should be returned in descending order (order is by timestamp). Default is ascending (by timestamp). This impacts the side that `offset` (skip) from `limit` is indexed from."
                example = false
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Successful query"
                body<List<UsageEvent>> {
                    description = "The list of events matching the supplied filters"
                    example("Example UsageEvent list", usageEventListExample)
                }
            }
        }
    }) {
        val callAuth = authorizationFromCall()
        val queryParams = queryParams()

        val projectId = getProjectId()

        val filterEvents = queryParams.getAll("filterEvents")?.apply {
            require(size <= 99) { "Filtered for too many events, max. 99 types" }
            require(isNotEmpty()) { "Added filter but no event type filtered for (always empty list)" }
        }

        val limit = queryParams["limit"]?.let {
            it.split(",").let { Pair(it.first().toInt(), it.last().toInt()) }
        }?.apply {
            require(first >= 0) { "Limit offset was set to a negative value" }
            require(second <= 5000) { "Limit exceeded, max. 5000 events in one go" }
            require(second > 0) { "Limit was set to 0 or a negative value" }
        } ?: Pair(0, 1000)


        val duringTime = queryParams["duringTime"]?.let {
            it.split(",").let {
                Pair(LocalDateTime.parse(it.first()), LocalDateTime.parse(it.last()))
            }
        }

        val orderDescending = queryParams["orderDescending"]?.let { it.toBoolean() } ?: false

        val events = EventService.listEvents(
            organizationId = callAuth.organizationId,
            projectId = projectId,
            filterEvents = filterEvents,
            limit = limit,
            duringTime = duringTime,
            orderDescending = orderDescending
        )

        context.respond(events)
    }

    route("push", {
        summary = "Push a new block of event occurrences."
        description = """
            Push a new block of events to the store.
            The organization ID for the events will be automatically determined by the API token.
            If the API token is locked to an specific project, the events will be checked to also be associated with that project.
        """.trimIndent()
    }) {

        suspend fun PipelineContext<Unit, ApplicationCall>.processEvents(userProvidedEvents: List<InsertUsageEvent>) {
            val callAuth = authorizationFromCall()
            if (callAuth.allowedProjectId != null) {
                require(userProvidedEvents.all { it.group == callAuth.allowedProjectId || it.group == null }) { "An invalid project id was set with an event. This API key is only allowed to process project: ${callAuth.allowedProjectId}" }
            }

            val eventsToInsert = userProvidedEvents.map {
                runCatching {
                    if (it.group == null && callAuth.allowedProjectId != null) {
                        it.toNormalUsageEvent(callAuth.organizationId, callAuth.allowedProjectId)
                    } else it.toNormalUsageEvent(callAuth.organizationId)
                }.getOrNull()
            }

            val result = EventService.insertEvents(eventsToInsert)

            val failedEvents = eventsToInsert.count { it == null }
            val totalEvents = eventsToInsert.size

            context.respond(
                HttpStatusCode.Created,
                PushResult(
                    eventsOk = totalEvents,
                    eventsFailed = failedEvents,
                    eventsWritten = result?.writtenRows ?: -1,
                    totalBytes = result?.writtenBytes ?: -1
                )
            )
        }


        post("json", {
            request {
                body<ArrayList<InsertUsageEvent>> {
                    example("Example UsageEvent list", insertUsageEventListExample)
                }
            }
            response {
                HttpStatusCode.Created to {
                    body<PushResult>()
                }
            }
        }) {
            val callAuth = authorizationFromCall()
            val userProvidedEvents = call.receive<List<InsertUsageEvent>>()

            processEvents(userProvidedEvents)
        }

        post("csv", {
            description = """
            Push a new block of events to the store.
            The organization ID for the events will be automatically determined by the API token.
            If the API token is locked to an specific project, the events will be checked to also be associated with that project.
            The CSV file is read with the charset UTF-8, the quote character is '"', the escape sequence is the backward slash (\), delimiter is the tab character (\t) by default - this can be configured in the header to be e.g. ',' or ';'.
        """.trimIndent()

            request {
                headerParameter<String>("delimiter") {
                    required = false
                    example = ";"
                    description = """Delimiter character to use for the CSV input, by default the tab character (\t)"""
                }
                body<String> {
                    example(
                        "CSV example (semicolon seperated)", """
                        projectId;eventId;timestamp;eventName;isBillable;reference;properties
                        ${UUID.generateUUID()};${UUID.generateUUID()};${dateTimeExample()};WeatherForecast24hRequested;true;api1-key,127.0.0.100;{"key1": "value1"}
                        ${UUID.generateUUID()};${UUID.generateUUID()};${dateTimeExample()};WeatherForecast30dRequested;true;api2-key,127.0.0.200;{"key2": "value2"}
                    """.trimIndent()
                    )
                }
            }
        }) {
            val callAuth = authorizationFromCall()

            val stream = call.receiveStream()
            val delimiterParam = call.request.header("delimiter")?.firstOrNull()

            fun missingFromEntry(what: String, row: Map<String, String>): Nothing =
                error("$what is missing from entry: $row")

            var userProvidedEvents: List<InsertUsageEvent>? = null

            csvReader {
                skipEmptyLine = true
                delimiter = delimiterParam ?: '\t'
                escapeChar = '\\'
            }.open(stream) {
                userProvidedEvents = readAllWithHeaderAsSequence().map { row ->
                    InsertUsageEvent(
                        orgId = callAuth.organizationId,
                        group = UUID(row["projectId"] ?: missingFromEntry("Project id", row)),
                        id = UUID(row["eventId"] ?: missingFromEntry("Event id", row)),
                        timestamp = Instant.parse(row["timestamp"] ?: missingFromEntry("Timestamp", row)),
                        name = row["eventName"] ?: missingFromEntry("Event name", row),
                        billable = row["isBillable"]?.toBooleanStrictOrNull() ?: true,
                        reference = row["reference"],
                        properties = row["properties"]
                    )
                }.toList()
            }

            userProvidedEvents ?: error("User provided events not initialized")

            processEvents(userProvidedEvents!!)
        }
    }

}
