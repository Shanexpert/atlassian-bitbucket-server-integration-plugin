package com.atlassian.bitbucket.jenkins.internal.scm;

public class BitbucketSCMRepository {

    private final String credentialsId;
    private final String projectKey;
    private final String repositorySlug;
    private final String serverId;

    public BitbucketSCMRepository(
            String credentialsId, String projectKey, String repositorySlug, String serverId) {
        this.credentialsId = credentialsId;
        this.projectKey = projectKey;
        this.repositorySlug = repositorySlug;
        this.serverId = serverId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getRepositorySlug() {
        return repositorySlug;
    }

    public String getServerId() {
        return serverId;
    }
}
