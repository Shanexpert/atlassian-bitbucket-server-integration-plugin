package com.atlassian.bitbucket.jenkins.internal.trigger;

import hudson.model.Cause;

import static com.atlassian.bitbucket.jenkins.internal.trigger.Messages.BitbucketWebhookTriggerCause_withAuthor;

public class BitbucketWebhookTriggerCause extends Cause {

    private final BitbucketWebhookTriggerRequest triggerRequest;

    public BitbucketWebhookTriggerCause(BitbucketWebhookTriggerRequest triggerRequest) {
        this.triggerRequest = triggerRequest;
    }

    @Override
    public String getShortDescription() {
        return triggerRequest
                .getActor()
                .map(actor -> BitbucketWebhookTriggerCause_withAuthor(actor.getDisplayName()))
                .orElseGet(Messages::BitbucketWebhookTriggerCause_withoutAuthor);
    }
}
