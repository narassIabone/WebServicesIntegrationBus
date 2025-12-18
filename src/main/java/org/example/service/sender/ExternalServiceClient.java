package org.example.service.sender;

import org.example.model.core.Message;

public interface ExternalServiceClient {
    Message send(Message message, String endpointUrl) throws Exception;
}
