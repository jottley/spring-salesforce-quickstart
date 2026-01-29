package org.springframework.social.salesforce.quickstart;


import java.util.stream.Collectors;

import org.springframework.salesforce.api.MissingAuthorizationException;
import org.springframework.salesforce.api.Salesforce;
import org.springframework.salesforce.api.SalesforceProfile;
import org.springframework.salesforce.api.SalesforceUserDetails;
import org.springframework.salesforce.connect.SalesforceConnectionFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/spring-security")
public class QuickstartSpringSecurityController
{
    private final SalesforceConnectionFactory salesforceConnectionFactory;
    private Salesforce salesforce;

    public QuickstartSpringSecurityController(SalesforceConnectionFactory salesforceConnectionFactory) {
        this.salesforceConnectionFactory = salesforceConnectionFactory;
    }

    @GetMapping("/")
    public String me(
        @RegisteredOAuth2AuthorizedClient("salesforce") OAuth2AuthorizedClient authorizedClient,
        @AuthenticationPrincipal(expression = "attributes['preferred_username'] ?: name") String username
    ) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String tokenType   = authorizedClient.getAccessToken().getTokenType().getValue();

        salesforce = salesforceConnectionFactory.createApi(accessToken);

        SalesforceUserDetails userDetails = salesforce.userOperations().getSalesforceUserDetails();

        return "User: " + username + "\nTokenType: " + tokenType + "\nAccessToken: " + accessToken +
               "\nSalesforce User ID: " + userDetails.getId() +
                "\nSalesforce PreferredUsername: " + userDetails.getPreferredUsername();
    }

    @GetMapping(path = "/accounts", produces = "application/json")
    public String getAccounts(@RegisteredOAuth2AuthorizedClient("salesforce") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        salesforce = salesforceConnectionFactory.createApi(accessToken);

       return salesforce.queryOperations().query("SELECT Id, Name FROM Account").getRecords().stream()
            .map(account -> "Account ID: " + account.getAttributes().get("Id") + ", Name: " + account.getAttributes().get("Name"))
            .collect(Collectors.joining("\n"));
    }

    @GetMapping("profile/test")
    public String getProfile() {

        if (salesforce == null) {
            return "Error: No Salesforce connection. Please authenticate first.";
        }

        return String.valueOf(salesforceConnectionFactory.getProfileMapper().test(salesforce));
    }

    @GetMapping(value = "profile/userDetails", produces = "application/json")
    public SalesforceUserDetails getUserDetails() {

        if (salesforce == null) {
            throw new MissingAuthorizationException("Error: No Salesforce connection. Please authenticate first.");
        }

        return salesforceConnectionFactory.getProfileMapper().getUserDetails(salesforce);
    }

    @GetMapping(value = "profile/userProfile", produces = "application/json")
    public SalesforceProfile getUserProfile() {

        if (salesforce == null) {
            throw new MissingAuthorizationException("Error: No Salesforce connection. Please authenticate first.");
        }

        return salesforceConnectionFactory.getProfileMapper().getUserProfile(salesforce);
    }
}
