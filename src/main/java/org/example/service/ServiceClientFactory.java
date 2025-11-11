package org.example.service;

public class ServiceClientFactory {

    public static ExternalServiceClient getClient(String protocol) {
        return switch (protocol.toUpperCase()) {
            case "REST" -> new RestServiceClient();
            //case "SOAP" -> new SoapServiceClient();
            case "HTTP" -> new HttpServiceClient();
            default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        };
    }
}
