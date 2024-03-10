package bms.buchhaltung.web.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS

fun CORSConfig.anyHeader(includeUnsafe: Boolean = false) {
    HttpHeaders.safeHeader.forEach { allowHeader(it) }
    if (includeUnsafe)
        HttpHeaders.unsafeHeader.forEach { allowHeader(it) }
}

val HttpHeaders.safeHeader
    get() = allHeaders.filter { !isUnsafe(it) }

val HttpHeaders.unsafeHeader
    get() = allHeaders.filter { isUnsafe(it) }

val HttpHeaders.allHeaders
    get() = listOf(
        Accept, AcceptCharset, AcceptEncoding, AcceptLanguage, AcceptRanges,
        Age, Allow, ALPN, AuthenticationInfo, Authorization, CacheControl, Connection,
        ContentDisposition, ContentEncoding, ContentLanguage, ContentLength, ContentLocation,
        ContentRange, ContentType, Cookie, DASL, Date, DAV, Depth, Destination, ETag, Expect,
        Expires, From, Forwarded, Host, HTTP2Settings, If, IfMatch, IfModifiedSince,
        IfNoneMatch, IfRange, IfScheduleTagMatch, IfUnmodifiedSince, LastModified,
        Location, LockToken, Link, MaxForwards, MIMEVersion, OrderingType, Origin,
        Overwrite, Position, Pragma, Prefer, PreferenceApplied, ProxyAuthenticate,
        ProxyAuthenticationInfo, ProxyAuthorization, PublicKeyPins, PublicKeyPinsReportOnly,
        Range, Referrer, RetryAfter, ScheduleReply, ScheduleTag, SecWebSocketAccept,
        SecWebSocketExtensions, SecWebSocketKey, SecWebSocketProtocol, SecWebSocketVersion,
        Server, SetCookie, SLUG, StrictTransportSecurity, TE, Timeout, Trailer,
        TransferEncoding, Upgrade, UserAgent, Vary, Via, Warning, WWWAuthenticate,
        AccessControlAllowOrigin, AccessControlAllowMethods, AccessControlAllowCredentials,
        AccessControlAllowHeaders, AccessControlRequestMethod, AccessControlRequestHeaders,
        AccessControlExposeHeaders, AccessControlMaxAge, XHttpMethodOverride, XForwardedHost,
        XForwardedServer, XForwardedProto, XForwardedFor, XRequestId, XCorrelationId
    )

fun Application.configureCORS() {

    fun CORSConfig.anyMethod() = HttpMethod.DefaultMethods.forEach { allowMethod(it) }


    /*install(CORS) { // WARNING: for security, do not include this

    }*/

    install(CORS) {
        anyHost()
        anyMethod()
        anyHeader()
    }
}
