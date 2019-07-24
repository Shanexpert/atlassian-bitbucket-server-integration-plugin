package com.atlassian.bitbucket.jenkins.internal.fixture;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketPluginConfiguration;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.ExtensionList;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.jvnet.hudson.test.JenkinsRule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class BitbucketJenkinsRule extends JenkinsRule {

    private static final String BITBUCKET_ADMIN_PASSWORD =
            System.getProperty("bitbucket.admin.password", "admin");
    private static final String BITBUCKET_ADMIN_USERNAME =
            System.getProperty("bitbucket.admin.username", "admin");
    private static final String BITBUCKET_BASE_URL =
            System.getProperty("bitbucket.baseurl", "http://localhost:7990/bitbucket");
    private static final AtomicReference<PersonalToken> PERSONAL_TOKEN = new AtomicReference<>();
    private BitbucketServerConfiguration bitbucketServer;
    private String credentialsId;
    private String serverId;

    @Override
    public void before() throws Throwable {
        super.before();

        serverId = UUID.randomUUID().toString();
        PersonalToken token = PERSONAL_TOKEN.get();
        if (token == null) {
            HashMap<String, Object> createTokenRequest = new HashMap<>();
            createTokenRequest.put("name", "BitbucketJenkinsRule-" + serverId);
            createTokenRequest.put("permissions", new String[]{"REPO_ADMIN"});
            ResponseBody<Response> tokenResponse =
                    RestAssured.given()
                            .log()
                            .ifValidationFails()
                            .auth()
                            .preemptive()
                            .basic(BITBUCKET_ADMIN_USERNAME, BITBUCKET_ADMIN_PASSWORD)
                            .contentType(ContentType.JSON)
                            .body(createTokenRequest)
                            .expect()
                            .statusCode(200)
                            .when()
                            .put(BITBUCKET_BASE_URL + "/rest/access-tokens/latest/users/admin")
                            .getBody();
            token = new PersonalToken(tokenResponse.path("id"), tokenResponse.path("token"));
            PERSONAL_TOKEN.set(token);
            Runtime.getRuntime().addShutdownHook(new BitbucketTokenCleanUpThread(token.getId()));
        }

        credentialsId = UUID.randomUUID().toString();
        setupCredentials(credentialsId, token.getSecret());
        bitbucketServer =
                new BitbucketServerConfiguration(credentialsId, BITBUCKET_BASE_URL, null, serverId);
        List<BitbucketServerConfiguration> servers = new ArrayList<>();
        servers.add(bitbucketServer);
        ExtensionList<BitbucketPluginConfiguration> configExtensions =
                jenkins.getExtensionList(BitbucketPluginConfiguration.class);
        BitbucketPluginConfiguration configuration = configExtensions.get(0);
        configuration.setServerList(servers);
        configuration.save();
    }

    public BitbucketServerConfiguration getBitbucketServer() {
        return bitbucketServer;
    }

    @Nonnull
    public String getCredentialsId() {
        return credentialsId;
    }

    @Nonnull
    public String getServerId() {
        return serverId;
    }

    private void setupCredentials(String credentialId, String secret) throws Exception {
        CredentialsStore store = CredentialsProvider.lookupStores(jenkins).iterator().next();
        Domain domain = Domain.global();
        Credentials credentials =
                new UsernamePasswordCredentialsImpl(
                        CredentialsScope.GLOBAL, credentialId, "", "admin", secret);
        store.addCredentials(domain, credentials);
    }

    private class BitbucketTokenCleanUpThread extends Thread {

        private final String tokenId;

        private BitbucketTokenCleanUpThread(String tokenId) {
            this.tokenId = tokenId;
        }

        @Override
        public void run() {
            RestAssured.given()
                    .log()
                    .all()
                    .auth()
                    .preemptive()
                    .basic(BITBUCKET_ADMIN_USERNAME, BITBUCKET_ADMIN_PASSWORD)
                    .contentType(ContentType.JSON)
                    .expect()
                    .statusCode(204)
                    .when()
                    .delete(
                            BITBUCKET_BASE_URL
                            + "/rest/access-tokens/latest/users/admin/"
                            + tokenId);
        }
    }

    private class PersonalToken {

        private final String id;
        private final String secret;

        public PersonalToken(String id, String secret) {
            this.id = id;
            this.secret = secret;
        }

        public String getId() {
            return id;
        }

        public String getSecret() {
            return secret;
        }
    }
}
