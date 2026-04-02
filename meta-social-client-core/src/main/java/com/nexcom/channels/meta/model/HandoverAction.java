package com.nexcom.channels.meta.model;

/**
 * Handover protocol actions for Messenger and Instagram DM.
 * Determines which Graph API endpoint to call.
 */
public enum HandoverAction {

    /**
     * Pass thread control to another app.
     * POST /{id}/pass_thread_control
     */
    PASS_THREAD("pass_thread_control"),

    /**
     * Take thread control from another app.
     * POST /{id}/take_thread_control
     */
    TAKE_THREAD("take_thread_control");

    private final String endpoint;

    HandoverAction(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
