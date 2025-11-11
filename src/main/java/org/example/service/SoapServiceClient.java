package org.example.service;

import org.example.model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Универсальный SOAP-клиент без привязки к конкретному WSDL.
 * Отправляет XML-запросы и получает XML-ответы.
 */
public class SoapServiceClient implements ExternalServiceClient {

    @Override
    public Message send(Message request, String endpointUrl) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Заголовки
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("Accept", "text/xml");

        // Если нужно — можно добавить SOAPAction
        connection.setRequestProperty("SOAPAction", "");

        // Отправляем XML в тело запроса
        try (OutputStream os = connection.getOutputStream()) {
            os.write(request.getPayload().getBytes());
            os.flush();
        }

        // Читаем ответ
        int statusCode = connection.getResponseCode();
        StringBuilder responseBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        statusCode >= 200 && statusCode < 400
                                ? connection.getInputStream()
                                : connection.getErrorStream()
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
        }

        // Формируем headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("statusCode", statusCode);
        headers.put("statusText", connection.getResponseMessage());
        connection.getHeaderFields().forEach((key, value) -> {
            if (key != null && value != null) headers.put(key, String.join(", ", value));
        });

        return new Message(responseBuilder.toString(), headers);
    }
}
