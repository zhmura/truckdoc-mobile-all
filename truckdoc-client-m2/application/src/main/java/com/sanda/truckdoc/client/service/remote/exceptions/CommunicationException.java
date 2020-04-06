package com.sanda.truckdoc.client.service.remote.exceptions;

/**
 * Any kind of network failure.
 *
 * @author Alexei Osipov
 */
public class CommunicationException extends RemoteCallException {

    public CommunicationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CommunicationException(String detailMessage) {
        super(detailMessage);
    }
}
