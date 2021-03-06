package com.atlassian.bitbucket.jenkins.internal.client;

import com.atlassian.bitbucket.jenkins.internal.client.exception.NotFoundException;

/**
 * Factory for Bitbucket Clients.
 */
public interface BitbucketClientFactory {

    /**
     * Construct a client that can retrieve the advertised capabilities from Bitbucket. The client
     * is thread safe and can be used multiple times.
     *
     * @return a client that is ready to use
     */
    BitbucketCapabilitiesClient getCapabilityClient();

    /**
     * Return a project client.
     *
     * @param projectKey key of the project, if a project with this key does not exist all
     *         subsequent calls on this client will throw a {@link NotFoundException}
     * @return a client that is ready to use
     */
    BitbucketProjectClient getProjectClient(String projectKey);

    /**
     * Return a project search client
     *
     * @return a client that is ready to use
     */
    BitbucketProjectSearchClient getProjectSearchClient();

    /**
     * Return a repository search client
     *
     * @param projectKey The project key to scope the repository search
     * @return a client that it ready to use
     */
    BitbucketRepositorySearchClient getRepositorySearchClient(String projectKey);

    /**
     * Return a client that can return the username for the credentials used.
     *
     * @return a client that is ready to use
     */
    BitbucketUsernameClient getUsernameClient();
}
