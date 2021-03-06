package com.atlassian.bitbucket.jenkins.internal.config;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class BitbucketTokenCredentialsImpl extends BaseStandardCredentials
        implements BitbucketTokenCredentials {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final Secret secret;

    @DataBoundConstructor
    public BitbucketTokenCredentialsImpl(
            @CheckForNull CredentialsScope scope,
            @CheckForNull String id,
            @CheckForNull String description,
            @Nonnull Secret secret) {
        super(scope, id, description);
        this.secret = secret;
    }

    @Override
    public Secret getSecret() {
        return secret;
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        static {
            IconSet.icons.addIcon(
                    new Icon(
                            "icon-bitbucket-credentials icon-sm",
                            "atlassian-bitbucket-server-scm/images/16x16/credentials.png",
                            Icon.ICON_SMALL_STYLE,
                            IconType.PLUGIN));
            IconSet.icons.addIcon(
                    new Icon(
                            "icon-bitbucket-credentials icon-md",
                            "atlassian-bitbucket-server-scm/images/24x24/credentials.png",
                            Icon.ICON_MEDIUM_STYLE,
                            IconType.PLUGIN));
            IconSet.icons.addIcon(
                    new Icon(
                            "icon-bitbucket-credentials icon-lg",
                            "atlassian-bitbucket-server-scm/images/32x32/credentials.png",
                            Icon.ICON_LARGE_STYLE,
                            IconType.PLUGIN));
            IconSet.icons.addIcon(
                    new Icon(
                            "icon-bitbucket-credentials icon-xlg",
                            "atlassian-bitbucket-server-scm/images/48x48/credentials.png",
                            Icon.ICON_XLARGE_STYLE,
                            IconType.PLUGIN));
        }

        @Override
        public String getDisplayName() {
            return "Bitbucket Admin Token";
        }

        @Override
        public String getIconClassName() {
            return "icon-bitbucket-credentials";
        }
    }
}
