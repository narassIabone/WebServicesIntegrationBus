package org.example.service.sender;

import org.example.model.core.Message;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServiceClient implements ExternalServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Message send(Message message, String endpointUrl) throws Exception {
        // 1. Заголовки
        HttpHeaders headers = new HttpHeaders();
        if (message.getHeaders() != null) {
            message.getHeaders().forEach(headers::add);
        }

        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.TEXT_PLAIN);
        }

        // 2. Определяем метод (по умолчанию POST)
        String methodName = headers.getFirst("X-HTTP-Method");
        HttpMethod method = HttpMethod.POST;
        if (methodName != null) {
            switch (methodName.toUpperCase()) {
                case "GET" -> method = HttpMethod.GET;
                case "PUT" -> method = HttpMethod.PUT;
                case "DELETE" -> method = HttpMethod.DELETE;
                case "PATCH" -> method = HttpMethod.PATCH;
            }
        }

        // 3. Тело запроса
        String payload = message.getPayload() != null ? message.getPayload() : "";
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        // 4. Выполнение запроса
        ResponseEntity<byte[]> response = restTemplate.exchange(
                endpointUrl,
                method,
                request,
                byte[].class
        );

        // 5. Формируем ответное сообщение
        String responseBody = response.getBody() != null
                ? new String(response.getBody(), StandardCharsets.UTF_8)
                : "";

        Message responseMessage = new Message(responseBody);

        // 6. Заголовки ответа
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("statusCode", String.valueOf(response.getStatusCodeValue()));
        responseHeaders.put("statusText", response.getStatusCode().toString());

        response.getHeaders().forEach((k, v) -> responseHeaders.put(k, String.join(",", v)));

        responseMessage.setHeaders(responseHeaders);

        return responseMessage;
    }
}
