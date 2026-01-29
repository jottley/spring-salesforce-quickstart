package org.springframework.social.salesforce.quickstart;


import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.salesforce.api.MissingAuthorizationException;
import org.springframework.salesforce.api.Salesforce;
import org.springframework.salesforce.api.SalesforceProfile;
import org.springframework.salesforce.api.SalesforceUserDetails;
import org.springframework.salesforce.connect.AccessGrant;
import org.springframework.salesforce.connect.SalesforceProfileMapper;
import org.springframework.salesforce.connect.SalesforceServiceProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/spring-salesforce")
public class QuickstartSpringSalesforceController
{

    private final SalesforceProfileMapper salesforceProfileMapper;
    private final SalesforceServiceProvider salesforceServiceProvider;

     private Salesforce salesforce;

    @Value("${spring.salesforce.redirect-uri}")
    private String redirectUri;

    @Value("${spring.salesforce.scope}")
    private String scope;

    public QuickstartSpringSalesforceController(SalesforceServiceProvider salesforceServiceProvider, SalesforceProfileMapper salesforceProfileMapper) {
        this.salesforceServiceProvider = salesforceServiceProvider;
        this.salesforceProfileMapper = salesforceProfileMapper;
    }

    @GetMapping("/authenticate")
    public String getAuthUrl() {
        return salesforceServiceProvider.getOAuth2Client().buildAuthorizeUrl(redirectUri, scope, null);
    }

    @GetMapping("/oauth2/code")
    public String oauth2Callback(@RequestParam("code") String authorizationCode) {

        AccessGrant accessGrant = salesforceServiceProvider.getOAuth2Client()
            .exchangeForAccess(authorizationCode, redirectUri);

        String accessToken = accessGrant.getAccessToken();

        String refreshToken = accessGrant.getRefreshToken() != null
            ? accessGrant.getRefreshToken()
            : null;

        salesforce = salesforceServiceProvider.getApi(accessToken);

        SalesforceUserDetails userDetails = salesforce.userOperations().getSalesforceUserDetails();

        return "Salesforce User ID: " + userDetails.getId() +
               "\n Salesforce Username: " + userDetails.getPreferredUsername() +
               "\n Access Token: " + accessToken +
               "\n Refresh Token: " + refreshToken;
    }

    @GetMapping(path = "/accounts", produces = "application/json")
    public String getAccounts() {

        if (salesforce == null) {
            return "Error: No Salesforce connection. Please authenticate first.";
        }

       return salesforce.queryOperations().query("SELECT Id, Name FROM Account").getRecords().stream()
            .map(account -> "Account ID: " + account.getAttributes().get("Id") + ", Name: " + account.getAttributes().get("Name"))
            .collect(Collectors.joining("\n"));
    }

    @GetMapping("/refresh-token")
    public String refreshToken(@RequestParam("refresh_token") String refreshToken) {
        AccessGrant accessGrant = salesforceServiceProvider.getOAuth2Client()
            .refreshAccess(refreshToken);

        String newAccessToken = accessGrant.getAccessToken();

        salesforce = salesforceServiceProvider.getApi(newAccessToken);

        SalesforceUserDetails userDetails = salesforce.userOperations().getSalesforceUserDetails();

        return "Salesforce User ID: " + userDetails.getId() +
               "\n Salesforce Username: " + userDetails.getPreferredUsername() +
               "\n New Access Token: " + newAccessToken;
    }

    @GetMapping("profile/test")
    public String getProfile() {

        if (salesforce == null) {
            return "Error: No Salesforce connection. Please authenticate first.";
        }

        return String.valueOf(salesforceProfileMapper.test(salesforce));
    }

    @GetMapping(value = "profile/userDetails", produces = "application/json")
    public SalesforceUserDetails getUserDetails() {

        if (salesforce == null) {
            throw new MissingAuthorizationException("Error: No Salesforce connection. Please authenticate first.");
        }

        return salesforceProfileMapper.getUserDetails(salesforce);
    }

    @GetMapping(value = "profile/userProfile", produces = "application/json")
    public SalesforceProfile getUserProfile() {

        if (salesforce == null) {
            throw new MissingAuthorizationException("Error: No Salesforce connection. Please authenticate first.");
        }

        return salesforceProfileMapper.getUserProfile(salesforce);
    }

}
