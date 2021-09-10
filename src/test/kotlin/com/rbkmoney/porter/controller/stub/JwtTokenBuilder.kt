package com.rbkmoney.porter.controller.stub

import com.rbkmoney.porter.service.IdGenerator
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.PrivateKey
import java.time.Instant
import java.util.UUID

class JwtTokenBuilder(val userId: String, val username: String, val email: String, val privateKey: PrivateKey?) {

    constructor(privateKey: PrivateKey?) : this(
        IdGenerator.randomString(),
        DEFAULT_USERNAME,
        DEFAULT_EMAIL,
        privateKey
    ) {
    }

    fun generateJwtWithRoles(issuer: String?, vararg roles: String?): String {
        val iat: Long = Instant.now().getEpochSecond()
        val exp = iat + 60 * 10
        return generateJwtWithRoles(iat, exp, issuer, *roles)
    }

    fun generateJwtWithRoles(
        iat: Long,
        exp: Long,
        issuer: String?,
        vararg roles: String?,
    ): String {
        val payload: String = try {
            JSONObject()
                .put("jti", UUID.randomUUID().toString())
                .put("exp", exp)
                .put("nbf", "0")
                .put("iat", iat)
                .put("iss", issuer)
                .put("aud", "private-api")
                .put("sub", userId)
                .put("typ", "Bearer")
                .put("azp", "private-api")
                .put(
                    "resource_access",
                    JSONObject()
                        .put(
                            "common-api",
                            JSONObject()
                                .put("roles", JSONArray(roles))
                        )
                )
                .put("preferred_username", username)
                .put("email", email).toString()
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return Jwts.builder()
            .setPayload(payload)
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact()
    }

    companion object {
        const val DEFAULT_USERNAME = "Darth Vader"
        const val DEFAULT_EMAIL = "darkside-the-best@mail.com"
    }
}
