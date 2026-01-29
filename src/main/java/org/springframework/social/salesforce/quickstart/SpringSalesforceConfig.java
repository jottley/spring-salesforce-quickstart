package org.springframework.social.salesforce.quickstart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.salesforce.connect.SalesforceConnectionFactory;
import org.springframework.salesforce.connect.SalesforceProfileMapper;
import org.springframework.salesforce.connect.SalesforceServiceProvider;

@Configuration
public class SpringSalesforceConfig {

    private final SalesforceServiceProvider salesforceServiceProvider;
    private final SalesforceProfileMapper salesforceProfileMapper;


    public SpringSalesforceConfig(
            @Value("${spring.salesforce.consumer.key}") String consumerKey,
            @Value("${spring.salesforce.consumer.secret}") String consumerSecret) {
        SalesforceConnectionFactory salesforceConnectionFactory = new SalesforceConnectionFactory(
            consumerKey,
            consumerSecret
        );
        this.salesforceServiceProvider = salesforceConnectionFactory.getServiceProvider();
        this.salesforceProfileMapper = salesforceConnectionFactory.getProfileMapper();
    }

    @Bean
    public SalesforceServiceProvider salesforceSericeProvider() {
        return salesforceServiceProvider;
    }

    @Bean
    public SalesforceProfileMapper salesforceProfileMapper() {
        return salesforceProfileMapper;
    }
}
