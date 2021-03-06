package com.atlassian.bitbucket.jenkins.internal.scm;

import com.atlassian.bitbucket.jenkins.internal.client.BitbucketClientFactoryProvider;
import com.atlassian.bitbucket.jenkins.internal.client.exception.BitbucketClientException;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketPluginConfiguration;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRepository;
import com.atlassian.bitbucket.jenkins.internal.utils.CredentialUtils;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitTool;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.browser.Stash;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.scm.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BitbucketSCM extends SCM {

    // avoid a difficult upgrade task.
    private final List<BranchSpec> branches;
    private final List<GitSCMExtension> extensions;
    private final String gitTool;
    private final String id;
    private final List<BitbucketSCMRepository>
            scmRepositories; // this is to enable us to support future multiple repositories and
    private transient BitbucketClientFactoryProvider bitbucketClientFactoryProvider;
    private GitSCM gitSCM;

    @DataBoundConstructor
    public BitbucketSCM(
            String id,
            List<BranchSpec> branches,
            String credentialsId,
            List<GitSCMExtension> extensions,
            String gitTool,
            String projectKey,
            String repositorySlug,
            String serverId) {

        this.id = isBlank(id) ? UUID.randomUUID().toString() : id;
        if (isBlank(projectKey)) {
            throw new BitbucketSCMException("A project is required", "projectKey");
        }
        if (isBlank(repositorySlug)) {
            throw new BitbucketSCMException("A repository is required", "repositorySlug");
        }
        if (isBlank(serverId)) {
            throw new BitbucketSCMException("A server is required", "serverId");
        }

        scmRepositories = new ArrayList<>(1);
        scmRepositories.add(
                new BitbucketSCMRepository(credentialsId, projectKey, repositorySlug, serverId));
        this.gitTool = gitTool;
        this.branches = branches;
        this.extensions = extensions;
    }

    @CheckForNull
    @Override
    public SCMRevisionState calcRevisionsFromBuild(
            @Nonnull Run<?, ?> build,
            @Nullable FilePath workspace,
            @Nullable Launcher launcher,
            @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        return gitSCM.calcRevisionsFromBuild(build, workspace, launcher, listener);
    }

    @Override
    public void checkout(
            @Nonnull Run<?, ?> build,
            @Nonnull Launcher launcher,
            @Nonnull FilePath workspace,
            @Nonnull TaskListener listener,
            @CheckForNull File changelogFile,
            @CheckForNull SCMRevisionState baseline)
            throws IOException, InterruptedException {
        gitSCM.checkout(build, launcher, workspace, listener, changelogFile, baseline);
    }

    @Override
    public PollingResult compareRemoteRevisionWith(
            @Nonnull Job<?, ?> project,
            @Nullable Launcher launcher,
            @Nullable FilePath workspace,
            @Nonnull TaskListener listener,
            @Nonnull SCMRevisionState baseline)
            throws IOException, InterruptedException {
        return gitSCM.compareRemoteRevisionWith(project, launcher, workspace, listener, baseline);
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return gitSCM.createChangeLogParser();
    }

    public void createGitSCM() {
        BitbucketSCMRepository scmRepository = getBitbucketSCMRepository();
        BitbucketServerConfiguration server = getServer();
        String repositorySlug = scmRepository.getRepositorySlug();

        try {
            String credentialsId = scmRepository.getCredentialsId();
            BitbucketRepository repo =
                    getRepository(
                            server, scmRepository.getProjectKey(), repositorySlug, credentialsId);
            UserRemoteConfig remoteConfig =
                    new UserRemoteConfig(
                            getCloneUrl(repo),
                            repositorySlug,
                            null,
                            pickCredentialsId(server, credentialsId));
            gitSCM =
                    new GitSCM(
                            Collections.singletonList(remoteConfig),
                            branches,
                            false,
                            Collections.emptyList(),
                            new Stash(getRepositoryUrl(repo)),
                            gitTool,
                            extensions);
        } catch (BitbucketClientException e) {
            throw new BitbucketSCMException(
                    "Failed to save configuration, please use the back button on your browser and try again. "
                    + "Additional information about this failure: "
                    + e.getMessage());
        }
    }

    public List<BranchSpec> getBranches() {
        return gitSCM.getBranches();
    }

    @CheckForNull
    @Override
    public RepositoryBrowser<?> getBrowser() {
        return gitSCM.getBrowser();
    }

    @CheckForNull
    public String getCredentialsId() {
        return getBitbucketSCMRepository().getCredentialsId();
    }

    public List<GitSCMExtension> getExtensions() {
        return gitSCM.getExtensions();
    }

    public String getGitTool() {
        return gitTool;
    }

    public String getId() {
        return id;
    }

    @CheckForNull
    public String getProjectKey() {
        return getBitbucketSCMRepository().getProjectKey();
    }

    @CheckForNull
    public String getRepositorySlug() {
        return getBitbucketSCMRepository().getRepositorySlug();
    }

    @CheckForNull
    public String getServerId() {
        return getBitbucketSCMRepository().getServerId();
    }

    public void setBitbucketClientFactoryProvider(
            BitbucketClientFactoryProvider bitbucketClientFactoryProvider) {
        this.bitbucketClientFactoryProvider = bitbucketClientFactoryProvider;
    }

    private static String getCloneUrl(BitbucketRepository repo) {
        return repo.getCloneUrls()
                .stream()
                .filter(link -> "http".equals(link.getName()))
                .findFirst()
                .orElseThrow(() -> new BitbucketClientException("No HttpClone url", -404, null))
                .getHref();
    }

    private static String getRepositoryUrl(BitbucketRepository repository) {
        String selfLink =
                repository.getSelfLink(); // self-link include /browse which needs to be trimmed
        return selfLink.substring(0, selfLink.length() - "/browse".length());
    }

    private BitbucketSCMRepository getBitbucketSCMRepository() {
        if (scmRepositories != null && scmRepositories.size() > 0) {
            return scmRepositories.get(0);
        } else {
            return new BitbucketSCMRepository("", "", "", "");
        }
    }

    private BitbucketRepository getRepository(
            BitbucketServerConfiguration server,
            String projectKey,
            String repositorySlug,
            String credentialsId) {
        return bitbucketClientFactoryProvider
                .getClient(server, CredentialUtils.getCredentials(credentialsId))
                .getProjectClient(projectKey)
                .getRepositoryClient(repositorySlug)
                .get();
    }

    private BitbucketServerConfiguration getServer() {
        return new BitbucketPluginConfiguration()
                .getServerById(getBitbucketSCMRepository().getServerId())
                .orElseThrow(() -> new RuntimeException("Server config not found"));
    }

    @Nullable
    private String pickCredentialsId(
            BitbucketServerConfiguration serverConfiguration, @Nullable String credentialsId) {
        return credentialsId != null ? credentialsId : serverConfiguration.getCredentialsId();
    }

    @Symbol("BbS")
    @Extension
    @SuppressWarnings({"MethodMayBeStatic", "unused"})
    public static class DescriptorImpl extends SCMDescriptor<BitbucketSCM> {

        private final GitSCM.DescriptorImpl gitScmDescriptor;
        @Inject
        private BitbucketClientFactoryProvider bitbucketClientFactoryProvider;

        public DescriptorImpl() {
            super(Stash.class);
            gitScmDescriptor = new GitSCM.DescriptorImpl();
            load();
        }

        public FormValidation doCheckProjectKey(@QueryParameter String value) {
            if (isEmpty(value)) {
                return FormValidation.error("Please specify a valid project key.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckRepositorySlug(@QueryParameter String value) {
            if (isEmpty(value)) {
                return FormValidation.error("Please specify a valid repository slug.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckServerId(@QueryParameter String serverId) {
            List<BitbucketServerConfiguration> serverList =
                    new BitbucketPluginConfiguration().getServerList();
            // Users can only demur in providing a server name if none are available to select
            if (!serverList.isEmpty() && isBlank(serverId)) {
                return FormValidation.error("Please specify a valid Bitbucket Server.");
            }
            return FormValidation.ok();
        }

        @SuppressWarnings({"Duplicates", "MethodMayBeStatic"})
        public ListBoxModel doFillCredentialsIdItems(
                @QueryParameter String baseUrl, @QueryParameter String credentialsId) {
            Jenkins instance = Jenkins.get();
            if (!instance.hasPermission(Jenkins.ADMINISTER)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }

            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            instance,
                            StringCredentials.class,
                            URIRequirementBuilder.fromUri(baseUrl).build(),
                            CredentialsMatchers.always())
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            instance,
                            StandardUsernamePasswordCredentials.class,
                            URIRequirementBuilder.fromUri(baseUrl).build(),
                            CredentialsMatchers.always());
        }

        public ListBoxModel doFillGitToolItems() {
            return gitScmDescriptor.doFillGitToolItems();
        }

        public ListBoxModel doFillServerIdItems(@QueryParameter String serverId) {
            List<BitbucketServerConfiguration> serverList =
                    new BitbucketPluginConfiguration().getServerList();
            StandardListBoxModel model = new StandardListBoxModel();
            if (isBlank(serverId) || serverList.isEmpty()) {
                model.includeEmptyValue();
            }

            model.addAll(
                    serverList
                            .stream()
                            .map(
                                    server ->
                                            new Option(
                                                    server.getServerName(),
                                                    server.getId(),
                                                    server.getId().equals(serverId)))
                            .collect(Collectors.toList()));
            return model;
        }

        @Override
        public String getDisplayName() {
            return "Bitbucket Server";
        }

        public List<GitSCMExtensionDescriptor> getExtensionDescriptors() {
            return gitScmDescriptor.getExtensionDescriptors();
        }

        public List<GitTool> getGitTools() {
            return gitScmDescriptor.getGitTools();
        }

        public boolean getShowGitToolOptions() {
            return gitScmDescriptor.showGitToolOptions();
        }

        /**
         * Overridden to provide a better experience for errors.
         *
         * @param req request
         * @param formData json data
         * @return a new BitbucketSCM instance
         * @throws FormException if any data is missing
         */
        @Override
        public SCM newInstance(@Nullable StaplerRequest req, @Nonnull JSONObject formData)
                throws FormException {
            try {
                BitbucketSCM scm = (BitbucketSCM) super.newInstance(req, formData);
                scm.setBitbucketClientFactoryProvider(bitbucketClientFactoryProvider);
                scm.createGitSCM();
                return scm;
            } catch (Error e) {
                Throwable cause = e.getCause();
                while (cause != null) {
                    if (cause instanceof BitbucketSCMException) {
                        throw new FormException(
                                cause.getMessage(), ((BitbucketSCMException) cause).getField());
                    }
                    cause = cause.getCause();
                }
                throw e; // didn't match any known error, so throw it up and let Jenkins handle it
            }
        }
    }
}
