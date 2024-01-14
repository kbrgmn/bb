package bms.usagebilling.web

import bms.usagebilling.service.events.EventService
import bms.usagebilling.service.events.InsertUsageEvent
import bms.usagebilling.service.events.UsageEvent
import bms.usagebilling.web.config.CallAuthentication.Companion.authorizationFromCall
import bms.usagebilling.web.config.UnauthorizedException
import bms.usagebilling.web.config.illegalArgument
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

    fun PipelineContext<Unit, ApplicationCall>.getGroupId(): UUID? {
        val requestedGroupIdString = queryParams()["group"]
            ?.let { runCatching { UUID(it) }.getOrElse { illegalArgument("Invalid group uuid: ${it.message}") } }

        val callAuth = authorizationFromCall()
        val groupId = when {
            callAuth.allowedGroupId == null && requestedGroupIdString == null -> null
            callAuth.allowedGroupId != null && requestedGroupIdString == null ->
                throw UnauthorizedException("Filter was not restricted to any group (all groups were requested), but API key is locked to group: ${callAuth.allowedGroupId}")

            callAuth.allowedGroupId == requestedGroupIdString -> callAuth.allowedGroupId
            callAuth.allowedGroupId == null -> requestedGroupIdString
            else -> error("Could not determine group id")
        }

        return groupId
    }

    get("query", {
        summary = "Query the organization or the group of the organization for a list of events, " +
                "optionally specify certain filters."
        description = """
            List all events of this organization that match a provided query.
            You can query by setting various filters as listed below.
            If you do not set a filter, the list will be unfiltered - thus all recorded events are returned.
            The default limit set is 1000 events per request. You can set an offset (to get the next 1000 events) with the `limit` filter.
            It is required to set the `group` filter if the API key is locked to an specific group. If the API key is not locked to a specific group, you are allowed to leave `group` empty, and thus query all events over all groups of the organization.
            """.trimIndent()
        request {
            queryParameter<String?>("group") {
                description =
                    "The `group` ID to filter by. Will list all groups if not set. If the API key is locked to a group, has to be set to that group."
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

        val groupId = getGroupId()

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
            organization = callAuth.organizationId,
            group = groupId,
            filterType = filterEvents,
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
            If the API token is locked to an specific group, the events will be checked to also be associated with that group.
        """.trimIndent()
    }) {

        suspend fun PipelineContext<Unit, ApplicationCall>.processEvents(userProvidedEvents: List<InsertUsageEvent>) {
            val callAuth = authorizationFromCall()
            if (callAuth.allowedGroupId != null) {
                require(userProvidedEvents.all { it.group == callAuth.allowedGroupId || it.group == null }) { "An invalid group id was set with an event. This API key is only allowed to process group: ${callAuth.allowedGroupId}" }
            }

            val eventsToInsert = userProvidedEvents.map {
                runCatching {
                    if (it.group == null && callAuth.allowedGroupId != null) {
                        it.toNormalUsageEvent(callAuth.organizationId, callAuth.allowedGroupId)
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
            val userProvidedEvents = call.receive<List<InsertUsageEvent>>()

            processEvents(userProvidedEvents)
        }

        post("csv", {
            description = """
            Push a new block of events to the store.
            The organization ID for the events will be automatically determined by the API token.
            If the API token is locked to an specific group, the events will be checked to also be associated with that group.
            The CSV file is read with the charset UTF-8, the quote character is '"', the escape sequence is the backward slash (\), delimiter is the tab character (\t) by default - this can be configured in the query to be e.g. ',' or ';'.
        """.trimIndent()

            request {
                queryParameter<String>("delimiter") {
                    required = false
                    example = ";"
                    description = """Delimiter character to use for the CSV input, by default the tab character (\t)"""
                }
                body<String> {
                    example(
                        "CSV example (semicolon seperated)", """
                        group;event;timestamp;type;billable;reference;properties
                        ${UUID.generateUUID()};${UUID.generateUUID()};${dateTimeExample()};WeatherForecast24hRequested;true;api1-key,127.0.0.100;{"key1": "value1"}
                        ${UUID.generateUUID()};${UUID.generateUUID()};${dateTimeExample()};WeatherForecast30dRequested;true;api2-key,127.0.0.200;{"key2": "value2"}
                    """.trimIndent()
                    )
                }
            }
        }) {
            val callAuth = authorizationFromCall()

            val stream = call.receiveStream()
            val delimiterParam = call.request.queryParameters["delimiter"]?.firstOrNull() ?: '\t'

            fun missingFromEntry(what: String, row: Map<String, String>): Nothing =
                error("$what is missing from entry: $row")

            var userProvidedEvents: List<InsertUsageEvent>? = null

            csvReader {
                skipEmptyLine = true
                delimiter = delimiterParam
                escapeChar = '\\'
            }.openAsync(stream) {
                userProvidedEvents = readAllWithHeaderAsSequence().map { row ->
                    InsertUsageEvent(
                        organization = callAuth.organizationId,
                        group = UUID(row["group"] ?: missingFromEntry("Group id", row)),
                        id = UUID(row["event"] ?: missingFromEntry("Event id", row)),
                        timestamp = Instant.parse(row["timestamp"] ?: missingFromEntry("Timestamp", row)),
                        type = row["type"] ?: missingFromEntry("Event name", row),
                        billable = row["billable"]?.toBooleanStrictOrNull() ?: true,
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
