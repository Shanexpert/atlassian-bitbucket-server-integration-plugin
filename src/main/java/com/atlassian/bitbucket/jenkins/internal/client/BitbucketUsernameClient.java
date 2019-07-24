package com.atlassian.bitbucket.jenkins.internal.client;

import java.util.Optional;

/**
 * Client to retrieve the username for the credentials used.
 */
public interface BitbucketUsernameClient extends BitbucketClient<Optional<String>> {
}
