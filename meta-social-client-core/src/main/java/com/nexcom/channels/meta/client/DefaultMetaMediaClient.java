package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.api.MetaMediaClient;
import com.nexcom.channels.meta.model.MediaUploadResponse;
import com.nexcom.channels.meta.model.MediaUrlResponse;
import com.nexcom.channels.meta.model.MetaApiContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Mono;

/**
 * Default implementation of {@link MetaMediaClient}.
 * Uses multipart upload for media, GET for URL retrieval, DELETE for removal.
 */
public class DefaultMetaMediaClient implements MetaMediaClient {

    private final MetaGraphApiClient graphApiClient;
    private final MetaEndpointResolver endpointResolver;
    private final MetaApiContext context;

    public DefaultMetaMediaClient(MetaGraphApiClient graphApiClient,
                                   MetaEndpointResolver endpointResolver,
                                   MetaApiContext context) {
        this.graphApiClient = graphApiClient;
        this.endpointResolver = endpointResolver;
        this.context = context;
    }

    @Override
    public Mono<MediaUploadResponse> upload(byte[] data, String filename, String contentType) {
        String url = endpointResolver.mediaAttachmentUrl(
                context.authPath(), context.apiVersion(), context.scopedId());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("message", "{\"attachment\":{\"type\":\"image\",\"payload\":{\"is_reusable\":true}}}",
                org.springframework.http.MediaType.APPLICATION_JSON);
        builder.part("filedata", new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        }).header(HttpHeaders.CONTENT_TYPE, contentType);

        return graphApiClient.postMultipart(url, token(), secret(), builder, MediaUploadResponse.class);
    }

    @Override
    public Mono<MediaUrlResponse> retrieveUrl(String attachmentId) {
        String url = endpointResolver.mediaUrl(context.authPath(), context.apiVersion(), attachmentId);
        return graphApiClient.get(url, token(), secret(), MediaUrlResponse.class);
    }

    @Override
    public Mono<Void> delete(String attachmentId) {
        String url = endpointResolver.mediaUrl(context.authPath(), context.apiVersion(), attachmentId);
        return graphApiClient.delete(url, token(), secret());
    }

    private String token() { return context.accessToken(); }
    private String secret() { return context.appSecret(); }
}
