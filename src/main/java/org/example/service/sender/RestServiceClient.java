package org.example.service.sender;

import org.example.model.core.Message;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class RestServiceClient implements ExternalServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public enum Format {
        JSON, XML
    }

    @Override
    public Message send(Message message, String endpointUrl) throws Exception {
        return send(message, endpointUrl, Format.JSON);
    }

    public Message send(Message message, String endpointUrl, Format format) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        if (format == Format.JSON) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        } else if (format == Format.XML) {
            headers.setContentType(MediaType.APPLICATION_XML);
        }

        // ИСПРАВЛЕНО: Тип теперь Map<String, Object>
        Map<String, Object> messageHeaders = message.getHeaders();
        if (messageHeaders != null) {
            // Преобразуем Object в String, так как HttpHeaders в Spring принимают только строки
            messageHeaders.forEach((key, value) -> {
                if (value != null) {
                    headers.add(key, value.toString());
                }
            });
        }

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);

        // Отправка POST запроса
        ResponseEntity<String> response = restTemplate.exchange(
                endpointUrl, HttpMethod.POST, request, String.class
        );

        // Формируем Message из ответа
        Message responseMessage = new Message(response.getBody());

        Map<String, Object> responseHeaders = new HashMap<>();
        responseHeaders.put("statusCode", response.getStatusCode().value());
        responseHeaders.put("statusText", response.getStatusCode().toString());

        responseMessage.setHeaders(responseHeaders);

        return responseMessage;
    }
}