package com.example.reposearcher.exception;
public class RateLimitException extends RuntimeException {
    private final String retryAfter;
    public RateLimitException(String m, String retryAfter) { super(m); this.retryAfter = retryAfter; }
    public String getRetryAfter() { return retryAfter; }
}
