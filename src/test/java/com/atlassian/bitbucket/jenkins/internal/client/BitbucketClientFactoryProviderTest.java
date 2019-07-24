package com.atlassian.bitbucket.jenkins.internal.client;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.cloudbees.plugins.credentials.Credentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketClientFactoryProviderTest {

    private BitbucketClientFactoryProvider clientFactoryProvider =
            new BitbucketClientFactoryProvider();

    @Test
    public void getClientWithoutCredentials() {
        BitbucketServerConfiguration config = mock(BitbucketServerConfiguration.class);
        when(config.getBaseUrl()).thenReturn("http://localhost");
        clientFactoryProvider.getClient(config, null);
        verify(config).getCredentials();
        verify(config).getBaseUrl();
        verifyNoMoreInteractions(config);
    }

    @Test
    public void getClient() {
        BitbucketServerConfiguration config = mock(BitbucketServerConfiguration.class);
        when(config.getBaseUrl()).thenReturn("http://localhost");
        clientFactoryProvider.getClient(config, mock(Credentials.class));
        verify(config).getBaseUrl();
        verifyNoMoreInteractions(config);
    }
}
