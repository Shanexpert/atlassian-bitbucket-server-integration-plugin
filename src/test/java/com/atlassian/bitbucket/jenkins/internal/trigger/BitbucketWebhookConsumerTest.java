package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.*;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import org.apache.groovy.util.Maps;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.bitbucket.jenkins.internal.trigger.BitbucketWebhookEndpoint.REFS_CHANGED_EVENT;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketWebhookConsumerTest {

    private static final String BB_CLONE_URL =
            "http://bitbucket.example.com/scm/jenkins/jenkins.git";
    private static final BitbucketUser BITBUCKET_USER =
            new BitbucketUser("admin", "admin@admin.com", "Admin User");

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();
    @Mock
    private BitbucketWebhookTriggerImpl bitbucketTrigger;
    private BitbucketWebhookConsumer consumer = new BitbucketWebhookConsumer();
    private FreeStyleProject gitProject;
    @Mock
    private GitSCM gitSCM;
    @Mock
    private BitbucketWebhookTriggerImpl nullBitbucketTrigger;
    private FreeStyleProject nullProject;
    @Mock
    private NullSCM nullSCM;
    private BitbucketRepository repository;

    @Before
    public void setup() throws Exception {
        gitProject = jenkins.createFreeStyleProject();
        gitProject.setScm(gitSCM);
        gitProject.addTrigger(bitbucketTrigger);
        RemoteConfig remoteConfig = mock(RemoteConfig.class);
        URIish uri = mock(URIish.class);
        when(uri.toString()).thenReturn(BB_CLONE_URL);
        when(remoteConfig.getURIs()).thenReturn(Collections.singletonList(uri));
        List<RemoteConfig> remoteConfigs = Collections.singletonList(remoteConfig);
        when(gitSCM.getRepositories()).thenReturn(remoteConfigs);

        nullProject = jenkins.createFreeStyleProject();
        nullProject.setScm(nullSCM);
        nullProject.addTrigger(nullBitbucketTrigger);

        repository = repository("http://bitbucket.example.com/scm/jenkins/jenkins.git");
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        gitProject.delete();
        nullProject.delete();
    }

    @Test
    public void testRefsChangedTriggerBuild() {
        RefsChangedWebhookEvent event =
                new RefsChangedWebhookEvent(
                        BITBUCKET_USER, REFS_CHANGED_EVENT, new Date(), refChanges(), repository);
        when(gitSCM.getBranches())
                .thenReturn(Collections.singletonList(new BranchSpec("**/master")));

        consumer.process(event);

        verify(bitbucketTrigger)
                .trigger(
                        eq(BitbucketWebhookTriggerRequest.builder().actor(BITBUCKET_USER).build()));
        verifyZeroInteractions(nullSCM);
        verify(nullBitbucketTrigger, never()).trigger(any());
    }

    @Test
    public void testShouldNotTriggerBuildIfConfiguredRefIsNotUpdated() {
        RefsChangedWebhookEvent event =
                new RefsChangedWebhookEvent(
                        BITBUCKET_USER, REFS_CHANGED_EVENT, new Date(), refChanges(), repository);
        when(gitSCM.getBranches())
                .thenReturn(Collections.singletonList(new BranchSpec("**/feature/*")));

        consumer.process(event);

        verify(bitbucketTrigger, never()).trigger(any());
    }

    @Test
    public void testShouldNotTriggerBuildIfRepositoryDoesNotMatch() {
        RefsChangedWebhookEvent event =
                new RefsChangedWebhookEvent(
                        BITBUCKET_USER, REFS_CHANGED_EVENT, new Date(), refChanges(), repository);

        consumer.process(event);

        verify(bitbucketTrigger, never()).trigger(any());
    }

    private List<BitbucketRefChange> refChanges() {
        BitbucketRef ref = new BitbucketRef("refs/heads/master", "master", BitbucketRefType.BRANCH);
        BitbucketRefChange change =
                new BitbucketRefChange(
                        ref, "refs/heads/master", "fromHash", "tohash", BitbucketRefChangeType.ADD);
        return Collections.singletonList(change);
    }

    private BitbucketRepository repository(String cloneUrl) {
        BitbucketNamedLink cloneLink = new BitbucketNamedLink("http", cloneUrl);
        List<BitbucketNamedLink> cloneLinks = Collections.singletonList(cloneLink);
        Map<String, List<BitbucketNamedLink>> links = Maps.of("clone", cloneLinks);
        BitbucketProject project = new BitbucketProject("jenkins", "jenkins");
        return new BitbucketRepository(
                "jenkins", links, project, "Jenkins", RepositoryState.AVAILABLE);
    }
}
