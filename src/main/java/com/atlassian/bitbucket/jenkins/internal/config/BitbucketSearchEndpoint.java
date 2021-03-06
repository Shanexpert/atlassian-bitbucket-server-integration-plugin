package com.atlassian.bitbucket.jenkins.internal.config;

import com.atlassian.bitbucket.jenkins.internal.client.BitbucketClientFactoryProvider;
import com.atlassian.bitbucket.jenkins.internal.client.BitbucketProjectSearchClient;
import com.atlassian.bitbucket.jenkins.internal.client.BitbucketRepositorySearchClient;
import com.atlassian.bitbucket.jenkins.internal.client.exception.BitbucketClientException;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketPage;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketProject;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRepository;
import com.atlassian.bitbucket.jenkins.internal.utils.CredentialUtils;
import com.cloudbees.plugins.credentials.Credentials;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.GET;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.kohsuke.stapler.HttpResponses.error;

@Extension
public class BitbucketSearchEndpoint implements RootAction {

    public static final String BITBUCKET_SERVER_SEARCH_URL = "bitbucket-server-search";

    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketSearchEndpoint.class);

    private BitbucketClientFactoryProvider bitbucketClientFactoryProvider;
    private BitbucketPluginConfiguration bitbucketPluginConfiguration;

    @GET
    public HttpResponse doFindProjects(
            @Nullable @QueryParameter("serverId") String serverId,
            @Nullable @QueryParameter("credentialsId") String credentialsId,
            @Nullable @QueryParameter("name") String name) {
        BitbucketProjectSearchClient projectSearchClient =
                bitbucketClientFactoryProvider
                        .getClient(getServer(serverId), getCredentials(credentialsId))
                        .getProjectSearchClient();
        try {
            BitbucketPage<BitbucketProject> projects =
                    projectSearchClient.get(StringUtils.stripToEmpty(name));
            return HttpResponses.okJSON(JSONObject.fromObject(projects));
        } catch (BitbucketClientException e) {
            // Something wen wrong with the request to Bitbucket
            LOGGER.error(e.getMessage());
            throw error(HTTP_INTERNAL_ERROR, e);
        }
    }

    @GET
    public HttpResponse doFindRepositories(
            @Nullable @QueryParameter("serverId") String serverId,
            @Nullable @QueryParameter("credentialsId") String credentialsId,
            @Nullable @QueryParameter("projectKey") String projectKey,
            @Nullable @QueryParameter("filter") String filter) {
        if (StringUtils.isBlank(projectKey)) {
            throw error(HTTP_BAD_REQUEST, "The projectKey must be present");
        }
        BitbucketRepositorySearchClient searchClient =
                bitbucketClientFactoryProvider
                        .getClient(getServer(serverId), getCredentials(credentialsId))
                        .getRepositorySearchClient(projectKey);
        try {
            BitbucketPage<BitbucketRepository> repositories =
                    searchClient.get(StringUtils.stripToEmpty(filter));
            return HttpResponses.okJSON(JSONObject.fromObject(repositories));
        } catch (BitbucketClientException e) {
            // Something wen wrong with the request to Bitbucket
            LOGGER.error(e.getMessage());
            throw error(HTTP_INTERNAL_ERROR, e);
        }
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return getClass().getName();
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return BITBUCKET_SERVER_SEARCH_URL;
    }

    @Inject
    public void setBitbucketClientFactoryProvider(
            BitbucketClientFactoryProvider bitbucketClientFactoryProvider) {
        this.bitbucketClientFactoryProvider = bitbucketClientFactoryProvider;
    }

    @Inject
    public void setBitbucketPluginConfiguration(
            BitbucketPluginConfiguration bitbucketPluginConfiguration) {
        this.bitbucketPluginConfiguration = bitbucketPluginConfiguration;
    }

    @Nullable
    private static Credentials getCredentials(
            @QueryParameter("credentialsId") @Nullable String credentialsId)
            throws HttpResponseException {
        Credentials credentials = null;
        if (!StringUtils.isBlank(credentialsId)) {
            credentials = CredentialUtils.getCredentials(credentialsId);
            if (credentials == null) {
                throw error(
                        HTTP_BAD_REQUEST,
                        "No corresponding credentials for the provided credentialsId");
            }
        }
        return credentials;
    }

    private BitbucketServerConfiguration getServer(
            @QueryParameter("serverId") @Nullable String serverId) {
        if (StringUtils.isBlank(serverId)) {
            throw error(
                    HTTP_BAD_REQUEST,
                    "A Bitbucket Server serverId must be provided as a query parameter");
        }
        return bitbucketPluginConfiguration
                .getServerById(serverId)
                .orElseThrow(
                        () ->
                                error(
                                        HTTP_BAD_REQUEST,
                                        "The provided Bitbucket Server serverId does not exist"));
    }
}
