package org.springframework.social.salesforce.quickstart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.salesforce.connect.SalesforceConnectionFactory;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    private final SalesforceConnectionFactory salesforceConnectionFactory;

    public SpringSecurityConfig(@Value("${spring.security.oauth2.client.registration.salesforce.client-id}") String consumerKey,
        @Value("${spring.security.oauth2.client.registration.salesforce.client-secret}") String consumerSecret) {

        this.salesforceConnectionFactory = new SalesforceConnectionFactory(consumerKey, consumerSecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
    {
        http
            .securityMatcher("/spring-security/**", "/oauth2/**", "/login/**")
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            .oauth2Login(withDefaults());
        return http.build();
    }

    @Bean
    public SalesforceConnectionFactory salesforceConnectionFactory() {
        return this.salesforceConnectionFactory;
    }

}
