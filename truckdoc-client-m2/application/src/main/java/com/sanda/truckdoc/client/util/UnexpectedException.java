package com.sanda.truckdoc.client.util;

/**
 * @author Alexei Osipov
 */
public class UnexpectedException extends RuntimeException {

    public UnexpectedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnexpectedException(String detailMessage) {
        super(detailMessage);
    }
}
