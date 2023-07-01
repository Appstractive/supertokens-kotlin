package com.supertokens.ktor.plugins

import com.supertokens.ktor.utils.UnauthorizedException
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.parseAuthorizationHeader


data class AuthenticatedUser(
    val id: String,
    val sessionHandle: String,
): Principal

const val SuperTokensAuth = "SuperTokens"

val TokenValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = {
    val sub = it.subject
    val sessionHandle = it["sessionHandle"]

    if(sub != null && sessionHandle != null) {
        AuthenticatedUser(id = sub, sessionHandle = sessionHandle)
    }
    else {
        null
    }

}

val authHeaderCookieWrapper: (ApplicationCall) -> HttpAuthHeader? = { call ->
    val authHeader = try {
        call.request.parseAuthorizationHeader()
    } catch (cause: IllegalArgumentException) {
        null
    }

    authHeader ?: call.request.cookies["sAccessToken"]?.let { HttpAuthHeader.Single("Bearer", it) }
}

inline fun <reified P : Principal> ApplicationCall.requirePrincipal(): P = requirePrincipal(null)
inline fun <reified P : Principal> ApplicationCall.requirePrincipal(provider: String?): P =
    authentication.principal(provider) ?: throw UnauthorizedException()