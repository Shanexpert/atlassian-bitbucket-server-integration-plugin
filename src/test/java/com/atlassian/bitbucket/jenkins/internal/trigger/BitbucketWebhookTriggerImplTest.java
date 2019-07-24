package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketUser;
import hudson.model.*;
import hudson.scm.PollingResult;
import jenkins.model.RunAction2;
import jenkins.triggers.SCMTriggerItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketWebhookTriggerImplTest {

    BitbucketWebhookTriggerImpl.BitbucketWebhookTriggerDescriptor descriptor =
            new BitbucketWebhookTriggerImpl.BitbucketWebhookTriggerDescriptor();
    BitbucketWebhookTriggerImpl trigger =
            new BitbucketWebhookTriggerImpl() {

                @Override
                public BitbucketWebhookTriggerImpl.BitbucketWebhookTriggerDescriptor
                getDescriptor() {
                    // There is no running Jenkins instance, so Trigger.getDescriptor won't work in
                    // the test.
                    return descriptor;
                }

                @Override
                public void trigger(BitbucketWebhookTriggerRequest triggerRequest) {
                    super.trigger(triggerRequest);

                    // The trigger is async because it puts the job on the queue and then the queue
                    // has to execute it.
                    // This can sometimes mean the test verifies if `scheduleBuild2` is called
                    // before the queue has actually
                    // had time to schedule it itself.
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    @Test
    public void testDescriptorIsApplicableForNonSCMTriggerItem() {
        assertFalse(descriptor.isApplicable(mock(Item.class)));
    }

    @Test
    public void testDescriptorIsApplicableForSCMTriggerItem() {
        assertTrue(
                descriptor.isApplicable(
                        mock(Item.class, withSettings().extraInterfaces(SCMTriggerItem.class))));
    }

    @Test
    public void testDescriptorIsApplicableForSCMedItem() {
        assertTrue(
                descriptor.isApplicable(
                        mock(Item.class, withSettings().extraInterfaces(SCMedItem.class))));
    }

    @Test
    public void testTriggerBuildNow() {
        FreeStyleProject project = mock(FreeStyleProject.class);
        Hudson itemGroup = mock(Hudson.class);
        when(itemGroup.getFullName()).thenReturn("Item name");
        when(project.getParent()).thenReturn(itemGroup);
        when(project.getName()).thenReturn("Project name");
        when(project.poll(any())).thenReturn(PollingResult.BUILD_NOW);
        trigger.start(project, true);

        trigger.trigger(
                BitbucketWebhookTriggerRequest.builder()
                        .actor(new BitbucketUser("me", "me@test.atlassian", "Me"))
                        .build());

        verify(project)
                .scheduleBuild2(
                        eq(0),
                        argThat(
                                (ArgumentMatcher<CauseAction>)
                                        argument ->
                                                "Triggered by Bitbucket webhook due to changes by Me."
                                                        .equals(
                                                                argument.getCauses()
                                                                        .get(0)
                                                                        .getShortDescription())));
    }

    @Test
    public void testTriggerNoChanges() {
        FreeStyleProject project = mock(FreeStyleProject.class);
        Hudson itemGroup = mock(Hudson.class);
        when(itemGroup.getFullName()).thenReturn("Item name");
        when(project.getParent()).thenReturn(itemGroup);
        when(project.getName()).thenReturn("Project name");
        when(project.poll(any())).thenReturn(PollingResult.NO_CHANGES);
        trigger.start(project, true);

        trigger.trigger(
                BitbucketWebhookTriggerRequest.builder()
                        .actor(new BitbucketUser("me", "me@test.atlassian", "Me"))
                        .build());

        verify(project).poll(any());
        verify(project, never()).scheduleBuild2(anyInt(), any(CauseAction.class));
    }

    @Test
    public void testTriggerSignificantChanges() throws Exception {
        FreeStyleProject project = mock(FreeStyleProject.class);
        Hudson itemGroup = mock(Hudson.class);
        when(itemGroup.getFullName()).thenReturn("Item name");
        when(project.getParent()).thenReturn(itemGroup);
        when(project.getName()).thenReturn("Project name");
        when(project.poll(any())).thenReturn(PollingResult.SIGNIFICANT);
        trigger.start(project, true);

        trigger.trigger(
                BitbucketWebhookTriggerRequest.builder()
                        .actor(new BitbucketUser("username", "me@test.atlassian", "me"))
                        .build());

        verify(project)
                .scheduleBuild2(
                        eq(0),
                        argThat(
                                (ArgumentMatcher<CauseAction>)
                                        argument ->
                                                "Triggered by Bitbucket webhook due to changes by me."
                                                        .equals(
                                                                argument.getCauses()
                                                                        .get(0)
                                                                        .getShortDescription())));
    }

    @Test
    public void testTriggerWithAdditionalActions() {
        FreeStyleProject project = mock(FreeStyleProject.class);
        Hudson itemGroup = mock(Hudson.class);
        when(itemGroup.getFullName()).thenReturn("Item name");
        when(project.getParent()).thenReturn(itemGroup);
        when(project.getName()).thenReturn("Project name");
        when(project.poll(any())).thenReturn(PollingResult.BUILD_NOW);
        trigger.start(project, true);
        RunAction2 additionalAction = mock(RunAction2.class);

        trigger.trigger(
                BitbucketWebhookTriggerRequest.builder()
                        .actor(new BitbucketUser("me", "me@test.atlassian", "Me"))
                        .additionalActions(additionalAction)
                        .build());

        verify(project)
                .scheduleBuild2(
                        eq(0),
                        argThat(
                                (ArgumentMatcher<CauseAction>)
                                        argument ->
                                                "Triggered by Bitbucket webhook due to changes by Me."
                                                        .equals(
                                                                argument.getCauses()
                                                                        .get(0)
                                                                        .getShortDescription())),
                        eq(additionalAction));
    }

    @Test
    public void testTriggerWithoutAuthor() {
        FreeStyleProject project = mock(FreeStyleProject.class);
        Hudson itemGroup = mock(Hudson.class);
        when(itemGroup.getFullName()).thenReturn("Item name");
        when(project.getParent()).thenReturn(itemGroup);
        when(project.getName()).thenReturn("Project name");
        when(project.poll(any())).thenReturn(PollingResult.SIGNIFICANT);
        trigger.start(project, true);

        trigger.trigger(BitbucketWebhookTriggerRequest.builder().build());

        verify(project)
                .scheduleBuild2(
                        eq(0),
                        argThat(
                                (ArgumentMatcher<CauseAction>)
                                        argument ->
                                                "Triggered by Bitbucket Server webhook."
                                                        .equals(
                                                                argument.getCauses()
                                                                        .get(0)
                                                                        .getShortDescription())));
    }
}
