package com.supertokens.ktor.plugins

import com.auth0.jwt.interfaces.Payload
import com.supertokens.ktor.utils.UnauthorizedException
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.util.AttributeKey


data class AuthenticatedUser(
    val id: String,
    val sessionHandle: String,
    val jwtPayload: Payload,
    val roles: Set<String>? = null,
    val permissions: Set<String>? = null,
) : Principal

const val SuperTokensAuth = "SuperTokens"

val TokenValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = {
    val sub = it.subject
    val sessionHandle = it["sessionHandle"]

    if (sub != null && sessionHandle != null) {
        AuthenticatedUser(
            id = sub,
            sessionHandle = sessionHandle,
            jwtPayload = it.payload,
            roles = it.payload.claims["st-role"]?.asList(String::class.java)?.toSet(),
            permissions = (it.payload.claims["st-perm"]?.asList(String::class.java)?.toSet()),
        )
    } else {
        null
    }

}

val authHeaderCookieWrapper: (ApplicationCall) -> HttpAuthHeader? = { call ->
    val authHeader = try {
        call.request.parseAuthorizationHeader()
    } catch (cause: IllegalArgumentException) {
        null
    }

    authHeader ?: call.request.cookies["sAccessToken"]?.let { HttpAuthHeader.Single("Bearer", it) }?.also {
        call.attributes.put(AccessTokenAttributeKey, it.blob)
    }
}

inline fun <reified P : Principal> ApplicationCall.requirePrincipal(): P = requirePrincipal(null)
inline fun <reified P : Principal> ApplicationCall.requirePrincipal(provider: String?): P =
    authentication.principal(provider) ?: throw UnauthorizedException()

val AccessTokenAttributeKey = AttributeKey<String>("AccessToken")

val ApplicationCall.accessToken: String get() = attributes[AccessTokenAttributeKey]