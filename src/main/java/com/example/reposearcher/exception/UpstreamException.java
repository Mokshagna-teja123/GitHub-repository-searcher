package com.example.reposearcher.exception;
public class UpstreamException extends RuntimeException {
    public UpstreamException(String m, Throwable c) { super(m, c); }
    public UpstreamException(String m) { super(m); }
}
