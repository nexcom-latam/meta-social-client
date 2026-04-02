package com.nexcom.channels.meta.model;

/**
 * Determines how the Instagram account was connected and which
 * Graph API base URL + token exchange flow to use.
 */
public enum InstagramAuthPath {

    /**
     * Instagram Login (preferred) — uses graph.instagram.com,
     * Instagram User Access Token, ig_exchange_token / ig_refresh_token.
     */
    INSTAGRAM_LOGIN,

    /**
     * Facebook Page-linked path (legacy) — uses graph.facebook.com,
     * Page Access Token, fb_exchange_token.
     */
    FACEBOOK_PAGE
}
