package com.rbkmoney.porter.service

import org.keycloak.KeycloakPrincipal
import org.keycloak.representations.AccessToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class KeycloakService {

    val partyId: String
        get() = (SecurityContextHolder.getContext().authentication.principal as KeycloakPrincipal<*>).name

    val accessToken: AccessToken
        get() {
            val keycloakPrincipal = SecurityContextHolder.getContext()
                .authentication
                .principal as KeycloakPrincipal<*>
            return keycloakPrincipal.keycloakSecurityContext.token
        }
}
