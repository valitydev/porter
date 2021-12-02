package com.rbkmoney.porter.config

import com.rbkmoney.porter.config.properties.KeycloakProperties
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.management.HttpSessionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@ComponentScan(
    basePackageClasses = [KeycloakSecurityComponents::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ["org.keycloak.adapters.springsecurity.management.HttpSessionManager"]
        )
    ]
)
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(value = ["auth.enabled"], havingValue = "true")
class SecurityConfig(
    private val keyCloakProperties: KeycloakProperties,
) : KeycloakWebSecurityConfigurerAdapter() {

    protected override fun httpSessionManager(): HttpSessionManager {
        return super.httpSessionManager()
    }

    @Bean
    protected override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return NullAuthenticatedSessionStrategy()
    }

    protected override fun configure(http: HttpSecurity) {
        super.configure(http)
        http.cors().and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers(HttpMethod.GET, "/**/health").permitAll()
            .anyRequest().authenticated()
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(keycloakAuthenticationProvider())
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.applyPermitDefaultValues()
        configuration.addAllowedMethod(HttpMethod.PUT)
        configuration.addAllowedMethod(HttpMethod.DELETE)
        configuration.addAllowedMethod(HttpMethod.PATCH)
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
