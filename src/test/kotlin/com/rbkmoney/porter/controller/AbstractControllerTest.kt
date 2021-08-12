package com.rbkmoney.porter.controller

import com.rbkmoney.porter.AbstractIntegrationTest
import com.rbkmoney.porter.controller.stub.KeycloakOpenIdStub
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc

@Import(KeycloakTestConfig::class)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = ["classpath:wiremock.properties"])
abstract class AbstractControllerTest : AbstractIntegrationTest() {

    @BeforeEach
    fun setUp() {
        keycloakOpenIdStub.givenStub()
    }

    @Autowired
    private lateinit var keycloakOpenIdStub: KeycloakOpenIdStub

    @Autowired
    protected lateinit var mockMvc: MockMvc

    protected fun generateJwt(iat: Long, exp: Long, vararg roles: String): String {
        return keycloakOpenIdStub.generateJwt(iat, exp, roles)
    }

    protected fun generateRbkAdminJwt(): String {
        return keycloakOpenIdStub.generateJwt("RBKadmin")
    }

    protected fun getUserFromToken(): String {
        return keycloakOpenIdStub.getUserId()
    }
}
