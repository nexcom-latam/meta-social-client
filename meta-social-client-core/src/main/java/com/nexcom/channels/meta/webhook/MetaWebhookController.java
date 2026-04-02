package com.nexcom.channels.meta.webhook;

import com.nexcom.channels.meta.api.MetaWebhookDispatcher;
import com.nexcom.channels.meta.exception.MetaWebhookSignatureException;
import com.nexcom.channels.meta.model.webhook.MetaWebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Webhook REST controller.
 * <ul>
 *   <li>GET: Hub challenge verification</li>
 *   <li>POST: Event reception — validate signature, parse, dispatch async, return 200 immediately</li>
 * </ul>
 */
@RestController
@RequestMapping("${nexcom.meta.webhook.path:/webhooks/meta}")
public class MetaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MetaWebhookController.class);

    private final MetaWebhookSignatureValidator signatureValidator;
    private final MetaWebhookParser parser;
    private final MetaWebhookDispatcher dispatcher;
    private final String verifyToken;

    public MetaWebhookController(MetaWebhookSignatureValidator signatureValidator,
                                 MetaWebhookParser parser,
                                 MetaWebhookDispatcher dispatcher,
                                 String verifyToken) {
        this.signatureValidator = signatureValidator;
        this.parser = parser;
        this.dispatcher = dispatcher;
        this.verifyToken = verifyToken;
    }

    /**
     * Hub challenge verification endpoint.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verification successful");
            return ResponseEntity.ok(challenge);
        }

        log.warn("Webhook verification failed: mode={}, token mismatch={}", mode, !verifyToken.equals(token));
        return ResponseEntity.status(403).build();
    }

    /**
     * Event reception endpoint. Returns 200 immediately, dispatches async.
     */
    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        try {
            signatureValidator.validate(rawBody, signature);
        } catch (MetaWebhookSignatureException e) {
            log.warn("Webhook signature validation failed: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        }

        List<MetaWebhookEvent> events = parser.parse(rawBody);
        log.debug("Parsed {} webhook events", events.size());

        // Fire-and-forget dispatch — don't block the 200 response
        for (MetaWebhookEvent event : events) {
            dispatcher.dispatch(event)
                    .subscribe(
                            null,
                            err -> log.error("Dispatch failed for mid={}", event.mid(), err)
                    );
        }

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MetaWebhookSignatureException.class)
    public ResponseEntity<Map<String, String>> handleSignatureError(MetaWebhookSignatureException e) {
        return ResponseEntity.status(403).body(Map.of("error", "invalid_signature"));
    }
}
