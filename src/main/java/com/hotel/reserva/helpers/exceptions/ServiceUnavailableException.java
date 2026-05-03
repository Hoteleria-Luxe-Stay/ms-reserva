package com.hotel.reserva.helpers.exceptions;

public class ServiceUnavailableException extends RuntimeException {

    private final String serviceName;

    public ServiceUnavailableException(String serviceName) {
        super("Service unavailable: " + serviceName);
        this.serviceName = serviceName;
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super("Service unavailable: " + serviceName, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
