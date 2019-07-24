package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketNamedLink;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRefChangeType;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.eclipse.jgit.transport.RemoteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class BitbucketWebhookConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketWebhookConsumer.class);

    void process(RefsChangedWebhookEvent event) {
        LOGGER.debug("Received refs changed event" + event);
        Set<String> cloneLinks = getCloneLinks(event);
        Set<String> refChanges = getRefChanges(event);

        try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
            Set<BitbucketWebhookTrigger> jobsToTrigger = new HashSet<>();
            for (ParameterizedJobMixIn.ParameterizedJob job :
                    Jenkins.get().getAllItems(ParameterizedJobMixIn.ParameterizedJob.class)) {
                BitbucketWebhookTrigger trigger = triggerFrom(job);
                if (trigger != null) {
                    Collection<? extends SCM> scms = getScms(job);
                    for (SCM scm : scms) {
                        if (scm instanceof GitSCM) {
                            GitSCM gitSCM = (GitSCM) scm;
                            List<RemoteConfig> repositories = gitSCM.getRepositories();
                            for (RemoteConfig repo : repositories) {
                                if (matchingRepo(cloneLinks, repo) && matchingRef(refChanges, gitSCM)) {
                                    LOGGER.debug("Triggering " + job.getFullDisplayName());
                                    jobsToTrigger.add(trigger);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            BitbucketWebhookTriggerRequest request =
                    BitbucketWebhookTriggerRequest.builder().actor(event.getActor()).build();
            jobsToTrigger.forEach(trigger -> trigger.trigger(request));
        }
    }

    void process(MirrorSynchronizedWebhookEvent event) {
        LOGGER.debug("Received mirror synchronized event" + event);
    }

    private static Set<String> getCloneLinks(RefsChangedWebhookEvent event) {
        return event.getRepository()
                .getCloneUrls()
                .stream()
                .map(BitbucketNamedLink::getHref)
                .collect(Collectors.toSet());
    }

    private static Set<String> getRefChanges(RefsChangedWebhookEvent event) {
        return event.getChanges()
                .stream()
                .filter(refChange -> refChange.getType() != BitbucketRefChangeType.DELETE)
                .map(refChange -> refChange.getRef().getId())
                .collect(Collectors.toSet());
    }

    private static Collection<? extends SCM> getScms(ParameterizedJobMixIn.ParameterizedJob job) {
        SCMTriggerItem triggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
        if (triggerItem != null) {
            Collection<? extends SCM> scms = triggerItem.getSCMs();
            return scms;
        }
        return Collections.emptySet();
    }

    private static boolean matchingRef(Set<String> refChanges, GitSCM gitSCM) {
        List<BranchSpec> branchSpecs = gitSCM.getBranches();
        return refChanges
                .stream()
                .anyMatch(
                        ref ->
                                branchSpecs
                                        .stream()
                                        .anyMatch(branchSpec -> branchSpec.matches(ref)));
    }

    private static boolean matchingRepo(Set<String> cloneLinks, RemoteConfig repo) {
        return repo.getURIs().stream().anyMatch(uri -> cloneLinks.contains(uri.toString()));
    }

    @Nullable
    private static BitbucketWebhookTriggerImpl triggerFrom(
            ParameterizedJobMixIn.ParameterizedJob item) {
        Map<TriggerDescriptor, Trigger<?>> triggers = item.getTriggers();
        for (Trigger candidate : triggers.values()) {
            if (candidate instanceof BitbucketWebhookTriggerImpl) {
                return (BitbucketWebhookTriggerImpl) candidate;
            }
        }
        return null;
    }
}
