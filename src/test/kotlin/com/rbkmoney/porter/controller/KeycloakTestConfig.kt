package com.rbkmoney.porter.controller

import com.rbkmoney.porter.controller.stub.JwtTokenBuilder
import com.rbkmoney.porter.controller.stub.KeycloakOpenIdStub
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.Properties

@TestConfiguration
class KeycloakTestConfig {
    @Bean
    fun keycloakOpenIdStub(
        @Value("\${keycloak.auth-server-url}") keycloakAuthServerUrl: String,
        @Value("\${keycloak.realm}") keycloakRealm: String,
        jwtTokenBuilder: JwtTokenBuilder,
    ): KeycloakOpenIdStub {
        return KeycloakOpenIdStub(keycloakAuthServerUrl, keycloakRealm, jwtTokenBuilder)
    }

    @Bean
    fun jwtTokenBuilder(keyPair: KeyPair): JwtTokenBuilder {
        return JwtTokenBuilder(keyPair.private)
    }

    @Bean
    fun keyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

//    @Bean
//    fun keycloakProperties(@Value("\${keycloak.auth-server-url}") keycloakAuthServerUrl: String): KeycloakProperties {
//        return KeycloakProperties().apply { authServerUrl = keycloakAuthServerUrl }
//    }

    companion object {
        @Bean
        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, IOException::class)
        fun properties(keyPair: KeyPair): PropertySourcesPlaceholderConfigurer {
            val fact = KeyFactory.getInstance("RSA")
            val spec = fact.getKeySpec(keyPair.public, X509EncodedKeySpec::class.java)
            val publicKey = Base64.getEncoder().encodeToString(spec.encoded)
            val pspc = PropertySourcesPlaceholderConfigurer()
            val properties = Properties()
            properties.load(ClassPathResource("application.yml").inputStream)
            properties.setProperty("keycloak.realm-public-key", publicKey)
            pspc.setProperties(properties)
            pspc.setLocalOverride(true)
            return pspc
        }
    }
}
