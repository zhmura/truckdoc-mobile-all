package com.sanda.truckdoc.client.service.remote.exceptions;

/**
 * @author Alexei Osipov
 */
public abstract class RemoteCallException extends Exception {

    protected RemoteCallException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    protected RemoteCallException(String detailMessage) {
        super(detailMessage);
    }
}
