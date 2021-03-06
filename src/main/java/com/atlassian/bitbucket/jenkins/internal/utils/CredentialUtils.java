package com.atlassian.bitbucket.jenkins.internal.utils;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import javax.annotation.CheckForNull;
import java.util.Collections;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class CredentialUtils {

    private CredentialUtils() {
        throw new UnsupportedOperationException(
                CredentialUtils.class.getName() + " should not be instantiated");
    }

    @CheckForNull
    public static Credentials getCredentials(String credentialsId) {
        Credentials creds = getCredentials(StringCredentials.class, credentialsId);

        if (creds == null) {
            creds = getCredentials(UsernamePasswordCredentials.class, credentialsId);
        }

        return creds;
    }

    @CheckForNull
    public static Credentials getCredentials(Class type, String credentialsId) {
        return firstOrNull(
                lookupCredentials(type, Jenkins.get(), ACL.SYSTEM, Collections.emptyList()),
                withId(trimToEmpty(credentialsId)));
    }
}
