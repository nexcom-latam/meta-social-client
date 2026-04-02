package com.nexcom.channels.meta.api;

import com.nexcom.channels.meta.model.MediaUploadResponse;
import com.nexcom.channels.meta.model.MediaUrlResponse;
import reactor.core.publisher.Mono;

/**
 * Client for media operations on Meta's Graph API.
 * Upload, retrieve temporary CDN URLs, and delete media attachments.
 */
public interface MetaMediaClient {

    /**
     * Uploads a media attachment for use in messaging.
     * Maps to POST /{id}/message_attachments (multipart).
     *
     * @param data        file bytes
     * @param filename    original filename
     * @param contentType MIME type (e.g., "image/jpeg")
     * @return attachment ID for use in SendRequest.mediaById()
     */
    Mono<MediaUploadResponse> upload(byte[] data, String filename, String contentType);

    /**
     * Retrieves the temporary CDN URL for a media attachment.
     * URLs are typically valid for ~5 minutes.
     * Maps to GET /{attachment-id}
     */
    Mono<MediaUrlResponse> retrieveUrl(String attachmentId);

    /**
     * Deletes a previously uploaded media attachment.
     * Maps to DELETE /{attachment-id}
     */
    Mono<Void> delete(String attachmentId);
}
