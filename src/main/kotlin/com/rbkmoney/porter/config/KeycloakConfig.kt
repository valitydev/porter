package com.rbkmoney.porter.config

import com.rbkmoney.porter.config.properties.KeycloakProperties
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(value = ["auth.enabled"], havingValue = "true")
class KeycloakConfig(
    private val keyCloakProperties: KeycloakProperties,
) {

    @Bean
    fun keycloakConfigResolver(): KeycloakConfigResolver {
        return KeycloakConfigResolver {
            val deployment: KeycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig())
            deployment.notBefore = keyCloakProperties.notBefore
            deployment
        }
    }

    private fun adapterConfig(): AdapterConfig {
        val keycloakRealmPublicKey: String = if (!keyCloakProperties.realmPublicKeyFilePath.isNullOrEmpty()) {
            readKeyFromFile(keyCloakProperties.realmPublicKeyFilePath!!)
        } else {
            keyCloakProperties.realmPublicKey
        }
        val adapterConfig = AdapterConfig().apply {
            realm = keyCloakProperties.realm
            realmKey = keycloakRealmPublicKey
            resource = keyCloakProperties.resource
            authServerUrl = keyCloakProperties.authServerUrl
            isUseResourceRoleMappings = true
            isBearerOnly = true
            sslRequired = keyCloakProperties.sslRequired
        }
        return adapterConfig
    }

    private fun readKeyFromFile(filePath: String): String {
        val strings = Files.readAllLines(Paths.get(filePath))
        strings.removeAt(strings.size - 1)
        strings.removeAt(0)

        return strings.stream().map(String::trim).collect(Collectors.joining())
    }
}
