package com.nexcom.channels.meta.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.api.MetaSocialClientFactory;
import com.nexcom.channels.meta.api.MetaWebhookDispatcher;
import com.nexcom.channels.meta.api.MetaWebhookEventHandler;
import com.nexcom.channels.meta.client.*;
import com.nexcom.channels.meta.metrics.MetaClientMetrics;
import com.nexcom.channels.meta.webhook.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(MetaSocialClientProperties.class)
@ConditionalOnProperty(prefix = "nexcom.meta", name = "app-secret")
public class MetaSocialClientAutoConfiguration {

    // ─── Phase 1: Webhook beans ───

    @Bean
    @ConditionalOnMissingBean
    public MetaWebhookSignatureValidator metaWebhookSignatureValidator(MetaSocialClientProperties props) {
        return new MetaWebhookSignatureValidator(props.appSecret());
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaWebhookParser metaWebhookParser(ObjectMapper objectMapper) {
        return new MetaWebhookParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaWebhookDispatcher metaWebhookDispatcher(List<MetaWebhookEventHandler<?>> handlers) {
        return new DefaultMetaWebhookDispatcher(handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaWebhookController metaWebhookController(
            MetaWebhookSignatureValidator validator,
            MetaWebhookParser parser,
            MetaWebhookDispatcher dispatcher,
            MetaSocialClientProperties props) {
        String verifyToken = props.webhook() != null ? props.webhook().verifyToken() : "";
        return new MetaWebhookController(validator, parser, dispatcher, verifyToken != null ? verifyToken : "");
    }

    // ─── Phase 1: HTTP client beans ───

    @Bean
    @ConditionalOnMissingBean
    public MetaEndpointResolver metaEndpointResolver() {
        return new MetaEndpointResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaApiErrorHandler metaApiErrorHandler(ObjectMapper objectMapper) {
        return new MetaApiErrorHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaGraphApiClient metaGraphApiClient(MetaApiErrorHandler errorHandler, ObjectMapper objectMapper) {
        return new WebClientMetaGraphApiClient(
                new MetaWebClientConfiguration(objectMapper).createWebClient(),
                errorHandler,
                objectMapper,
                MetaRetryFilter.defaultRetry()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaSocialClientFactory metaSocialClientFactory(MetaGraphApiClient graphApiClient,
                                                           MetaEndpointResolver endpointResolver) {
        return new DefaultMetaSocialClientFactory(graphApiClient, endpointResolver);
    }

    // ─── Metrics (optional, requires Micrometer on classpath) ───

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public MetaClientMetrics metaClientMetrics(MeterRegistry registry) {
        return new MetaClientMetrics(registry);
    }
}
