package com.rbkmoney.porter.controller.stub

import com.github.tomakehurst.wiremock.client.WireMock

class KeycloakOpenIdStub(
    keycloakAuthServerUrl: String,
    private val keycloakRealm: String,
    private val jwtTokenBuilder: JwtTokenBuilder,
) {
    private val issuer: String = "$keycloakAuthServerUrl/realms/$keycloakRealm"
    private val openidConfig: String =
        """{
            "issuer": "$issuer",
            "authorization_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/auth",
            "token_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/token",
            "token_introspection_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/token/introspect",
            "userinfo_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/userinfo",
            "end_session_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/logout",
            "jwks_uri": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/certs",
            "check_session_iframe": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/login-status-iframe.html",
            "registration_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/clients-registrations/openid-connect",
            "introspection_endpoint": "$keycloakAuthServerUrl/realms/$keycloakRealm/protocol/openid-connect/token/introspect"
        }"""

    fun givenStub() {
        WireMock.stubFor(
            WireMock.get(
                WireMock.urlEqualTo(
                    String.format(
                        "/auth/realms/%s/.well-known/openid-configuration",
                        keycloakRealm
                    )
                )
            )
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openidConfig)
                )
        )
    }

    fun generateJwt(vararg roles: String?): String {
        return jwtTokenBuilder.generateJwtWithRoles(issuer, *roles)
    }

    fun generateJwt(iat: Long, exp: Long, roles: Array<out String>): String {
        return jwtTokenBuilder.generateJwtWithRoles(iat, exp, issuer, *roles)
    }

    fun getUserId(): String {
        return jwtTokenBuilder.userId
    }
}
