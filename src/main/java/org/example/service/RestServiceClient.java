package org.example.service;

import org.example.model.Message;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class RestServiceClient implements ExternalServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public enum Format {
        JSON, XML
    }

    @Override
    public Message send(Message message, String endpointUrl) throws Exception {
        // По умолчанию определяем формат JSON
        return send(message, endpointUrl, Format.JSON);
    }

    public Message send(Message message, String endpointUrl, Format format) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        // Устанавливаем Content-Type в зависимости от формата
        if (format == Format.JSON) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        } else if (format == Format.XML) {
            headers.setContentType(MediaType.APPLICATION_XML);
        }

        // Добавляем пользовательские headers
        Map<String, String> messageHeaders = message.getHeaders();
        if (messageHeaders != null) {
            messageHeaders.forEach(headers::add);
        }

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);

        // Отправка POST запроса
        ResponseEntity<String> response = restTemplate.exchange(
                endpointUrl, HttpMethod.POST, request, String.class
        );

        // Формируем Message из ответа
        Message responseMessage = new Message(response.getBody());
        responseMessage.setHeaders(Map.of(
                "statusCode", String.valueOf(response.getStatusCodeValue()),
                "statusText", response.getStatusCode().toString()
        ));

        return responseMessage;
    }
}
