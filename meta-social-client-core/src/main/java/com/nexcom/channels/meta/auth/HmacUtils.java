package com.nexcom.channels.meta.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HMAC-SHA256 utilities for Meta Graph API appsecret_proof generation
 * and webhook signature validation.
 */
public final class HmacUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SHA256_PREFIX = "sha256=";

    private HmacUtils() {}

    /**
     * Computes HMAC-SHA256 of {@code data} using {@code secret} as the key.
     *
     * @return lowercase hex string
     */
    public static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }

    /**
     * Generates the appsecret_proof parameter for Graph API requests.
     * Mirrors {@code APIContext.sha256()} from facebook-java-business-sdk.
     */
    public static String appSecretProof(String accessToken, String appSecret) {
        return hmacSha256Hex(appSecret, accessToken);
    }

    /**
     * Validates the X-Hub-Signature-256 webhook header.
     * Strips the "sha256=" prefix, computes the expected hash, and
     * performs a constant-time comparison.
     *
     * @return false (does not throw) if the header is null, blank, or malformed
     */
    public static boolean verifyWebhookSignature(String rawBody, String xHubSignature256, String appSecret) {
        if (xHubSignature256 == null || xHubSignature256.isBlank()) {
            return false;
        }
        if (!xHubSignature256.startsWith(SHA256_PREFIX)) {
            return false;
        }

        String received = xHubSignature256.substring(SHA256_PREFIX.length());
        String expected = hmacSha256Hex(appSecret, rawBody);

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                received.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
