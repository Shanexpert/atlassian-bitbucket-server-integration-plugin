package com.atlassian.bitbucket.jenkins.internal.client;

import com.atlassian.bitbucket.jenkins.internal.client.exception.NotFoundException;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketProject;

/**
 * Client to get a project from the Bitbucket server.
 */
public interface BitbucketProjectClient extends BitbucketClient<BitbucketProject> {

    /**
     * Get a client for a repository. If no repository exists with the given slug all calls on the
     * returned client will throw {@link NotFoundException}
     *
     * @param slug the slug of the repository to use
     * @return repository client
     */
    BitbucketRepositoryClient getRepositoryClient(String slug);
}
