package com.atlassian.bitbucket.jenkins.internal.client.exception;

import javax.annotation.Nullable;

/**
 * The requested URL does not exist on the server.
 */
public class NotFoundException extends BitbucketClientException {

    public NotFoundException(String message, @Nullable String body) {
        super(message, 404, body);
    }
}
