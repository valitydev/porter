package com.rbkmoney.porter.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import kotlin.properties.Delegates

@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "keycloak")
class KeycloakProperties {
    lateinit var realm: String

    lateinit var resource: String

    lateinit var realmPublicKey: String

    var realmPublicKeyFilePath: String? = null

    lateinit var authServerUrl: String

    lateinit var sslRequired: String

    var notBefore by Delegates.notNull<Int>()
}
