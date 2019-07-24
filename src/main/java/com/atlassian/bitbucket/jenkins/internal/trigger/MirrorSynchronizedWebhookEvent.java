package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRefChange;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRepository;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MirrorSynchronizedWebhookEvent extends RefsChangedWebhookEvent {

    @JsonCreator
    public MirrorSynchronizedWebhookEvent(
            @JsonProperty(value = "actor") BitbucketUser actor,
            @JsonProperty(value = "eventKey", required = true) String eventKey,
            @JsonProperty(value = "date", required = true) Date date,
            @JsonProperty(value = "changes", required = true) List<BitbucketRefChange> changes,
            @JsonProperty(value = "repository", required = true) BitbucketRepository repository) {
        super(actor, eventKey, date, changes, repository);
    }
}
