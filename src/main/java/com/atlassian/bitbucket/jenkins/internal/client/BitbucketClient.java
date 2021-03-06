package com.atlassian.bitbucket.jenkins.internal.client;

import com.atlassian.bitbucket.jenkins.internal.client.exception.*;

/**
 * Basic Bitbucket client. A client is normally threadsafe and can be used multiple times.
 *
 * @param <T> the type to return
 */
public interface BitbucketClient<T> {

    /**
     * Make the call out to Bitbucket and read the response.
     *
     * @return the result of the call
     * @throws AuthorizationException if the credentials did not allow access to the given url
     * @throws NoContentException if the server did not respond with a body
     * @throws ConnectionFailureException if the server did not respond
     * @throws NotFoundException if the requested url does not exist
     * @throws BadRequestException if the request was malformed and thus rejected by the server
     * @throws ServerErrorException if the server failed to process the request
     * @throws BitbucketClientException for all errors not already captured
     */
    T get();
}
