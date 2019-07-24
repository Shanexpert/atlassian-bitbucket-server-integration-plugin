package com.atlassian.bitbucket.jenkins.internal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketProject {

    private final String key;
    private final String name;

    @JsonCreator
    public BitbucketProject(
            @JsonProperty(value = "key", required = true) String key,
            @JsonProperty(value = "name", required = true) String name) {
        this.key = requireNonNull(key, "key");
        this.name = requireNonNull(name, "name");
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
