package com.atlassian.bitbucket.jenkins.internal.config;

import com.atlassian.bitbucket.jenkins.internal.client.BitbucketClientFactoryProvider;
import hudson.util.FormValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketServerConfigurationTest {

    @Mock
    private BitbucketClientFactoryProvider clientFactoryProvider;

    @InjectMocks
    private BitbucketServerConfiguration.DescriptorImpl descriptor;

    @Test
    public void testCorrectUrl() {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckBaseUrl("http://localhost").kind);
    }

    @Test
    public void testEmptyBaseUrl() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckBaseUrl("").kind);
    }

    @Test
    public void testEmptyServerName() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckServerName("").kind);
    }

    @Test
    public void testFullUrl() {
        assertEquals(
                FormValidation.Kind.OK,
                descriptor.doCheckBaseUrl("http://localhost:7990/bitbucket").kind);
    }

    @Test
    public void testIPv6UrlWithoutProtocol() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckBaseUrl("::").kind);
    }

    @Test
    public void testMissingHost() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckBaseUrl("http://").kind);
    }

    @Test
    public void testMultipleTrailingSlashes() {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckBaseUrl("http://localhost//").kind);
    }

    @Test
    public void testNoContext() {
        assertEquals(
                FormValidation.Kind.OK, descriptor.doCheckBaseUrl("http://localhost:7990").kind);
    }

    @Test
    public void testNullBaseUrl() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckBaseUrl(null).kind);
    }

    @Test
    public void testNullServerName() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckServerName(null).kind);
    }

    @Test
    public void testTrailingSlash() {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckBaseUrl("http://localhost/").kind);
    }

    @Test
    public void testUrlWithoutProtocol() {
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckBaseUrl("localhost").kind);
    }

    @Test
    public void testValidIPV6Url() {
        assertEquals(
                FormValidation.Kind.OK,
                descriptor.doCheckBaseUrl("http://[fe80::ecdf:27ab:e5ec:20a6]:7990").kind);
    }

    @Test
    public void testValidIPv6LocalhostUrl() {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckBaseUrl("http://[::]:7990").kind);
    }

    @Test
    public void testValidServerName() {
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckServerName("Server Name").kind);
    }
}
