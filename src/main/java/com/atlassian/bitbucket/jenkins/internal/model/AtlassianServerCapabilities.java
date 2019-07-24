package com.atlassian.bitbucket.jenkins.internal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.Nullable;

/**
 * Capabilities as advertised by Atlassian products.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtlassianServerCapabilities {

    private String application;

    /**
     * Return the application type as provided by the remote side.
     *
     * @return the application type, or null if it was not part of the response
     */
    @Nullable
    public String getApplication() {
        return application;
    }

    /**
     * @return true if the server claims to be a Bitbucket server instance. False if the application
     *         type was not provided or if it is some other Atlassian server
     */
    public boolean isBitbucketServer() {
        return "stash".equals(application);
    }
}
