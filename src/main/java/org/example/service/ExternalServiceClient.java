package org.example.service;

import org.example.model.Message;

public interface ExternalServiceClient {
    Message send(Message message, String endpointUrl) throws Exception;
}
