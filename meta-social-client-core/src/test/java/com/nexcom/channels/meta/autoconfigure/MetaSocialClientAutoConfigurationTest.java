package com.nexcom.channels.meta.autoconfigure;

import com.nexcom.channels.meta.api.MetaSocialClientFactory;
import com.nexcom.channels.meta.api.MetaWebhookDispatcher;
import com.nexcom.channels.meta.client.MetaEndpointResolver;
import com.nexcom.channels.meta.client.MetaGraphApiClient;
import com.nexcom.channels.meta.webhook.MetaWebhookController;
import com.nexcom.channels.meta.webhook.MetaWebhookParser;
import com.nexcom.channels.meta.webhook.MetaWebhookSignatureValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MetaSocialClientAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    MetaSocialClientAutoConfiguration.class
            ));

    @Test
    void contextLoads_whenAppSecretPresent() {
        runner.withPropertyValues(
                "nexcom.meta.app-secret=test_secret",
                "nexcom.meta.app-id=test_app_id",
                "nexcom.meta.webhook.verify-token=test_verify"
        ).run(ctx -> {
            assertThat(ctx).hasSingleBean(MetaWebhookSignatureValidator.class);
            assertThat(ctx).hasSingleBean(MetaWebhookParser.class);
            assertThat(ctx).hasSingleBean(MetaWebhookDispatcher.class);
            assertThat(ctx).hasSingleBean(MetaWebhookController.class);
            assertThat(ctx).hasSingleBean(MetaEndpointResolver.class);
            assertThat(ctx).hasSingleBean(MetaGraphApiClient.class);
            assertThat(ctx).hasSingleBean(MetaSocialClientFactory.class);
        });
    }

    @Test
    void contextDoesNotLoad_whenAppSecretMissing() {
        runner.run(ctx -> {
            assertThat(ctx).doesNotHaveBean(MetaWebhookSignatureValidator.class);
            assertThat(ctx).doesNotHaveBean(MetaSocialClientFactory.class);
        });
    }
}
