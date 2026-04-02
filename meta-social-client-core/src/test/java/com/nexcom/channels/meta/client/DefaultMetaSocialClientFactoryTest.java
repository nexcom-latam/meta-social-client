package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.api.MetaMediaClient;
import com.nexcom.channels.meta.api.MetaMessagingClient;
import com.nexcom.channels.meta.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultMetaSocialClientFactoryTest {

    private final MetaGraphApiClient graphApiClient = mock(MetaGraphApiClient.class);
    private final MetaEndpointResolver endpointResolver = new MetaEndpointResolver();

    private final MetaApiContext context = new MetaApiContext(
            "tenant_1", MetaChannel.INSTAGRAM, InstagramAuthPath.INSTAGRAM_LOGIN,
            "token", "secret", MetaApiVersion.DEFAULT, "ig_123"
    );

    @Test
    void create_returnsMessagingClient() {
        var factory = new DefaultMetaSocialClientFactory(graphApiClient, endpointResolver);
        MetaMessagingClient client = factory.create(context);
        assertThat(client).isInstanceOf(DefaultMetaMessagingClient.class);
    }

    @Test
    void createMediaClient_returnsMediaClient() {
        var factory = new DefaultMetaSocialClientFactory(graphApiClient, endpointResolver);
        MetaMediaClient client = factory.createMediaClient(context);
        assertThat(client).isInstanceOf(DefaultMetaMediaClient.class);
    }
}
