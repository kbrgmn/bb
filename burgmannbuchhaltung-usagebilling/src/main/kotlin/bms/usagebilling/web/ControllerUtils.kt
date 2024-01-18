package bms.usagebilling.web

import bms.usagebilling.web.config.CallAuthentication.Companion.authorizationFromCall
import bms.usagebilling.web.config.UnauthorizedException
import bms.usagebilling.web.config.illegalArgument
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

internal fun PipelineContext<Unit, ApplicationCall>.queryParams() = call.request.queryParameters
internal fun dateTimeExample() = Clock.System.now().run {
    minus(nanosecondsOfSecond.nanoseconds).plus(123.milliseconds)
}

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
