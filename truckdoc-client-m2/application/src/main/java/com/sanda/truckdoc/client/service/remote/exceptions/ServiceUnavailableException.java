package com.sanda.truckdoc.client.service.remote.exceptions;

/**
 * Any kind of network failure.
 *
 * @author Alexei Osipov
 */
public class ServiceUnavailableException extends RemoteCallException {

    public ServiceUnavailableException(String detailMessage) {
        super(detailMessage);
    }
}
