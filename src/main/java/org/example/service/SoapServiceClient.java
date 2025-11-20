package org.example.service;

import org.example.model.Message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SoapServiceClient implements ExternalServiceClient {

    private static final int CONNECT_TIMEOUT = 5000; // 5 секунд
    private static final int READ_TIMEOUT = 30000;    // 30 секунд

    @Override
    public Message send(Message request, String endpointUrl) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Настройка тайм-аутов, чтобы не вешать потоки
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Заголовки
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("Accept", "text/xml");

        // Берем SOAPAction из заголовков сообщения, если есть, иначе пустой
        String soapAction = "";
        if (request.getHeaders() != null && request.getHeaders().containsKey("SOAPAction")) {
            soapAction = request.getHeaders().get("SOAPAction").toString();
        }
        connection.setRequestProperty("SOAPAction", soapAction);

        // Отправляем XML
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = request.getPayload().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }

        // Читаем код ответа
        int statusCode = connection.getResponseCode();

        // Выбираем правильный поток (обычный или error)
        InputStream streamToRead = (statusCode >= 200 && statusCode < 400)
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (streamToRead == null) {
            // Бывает, что ErrorStream пустой при 404 или 401
            return new Message("", Map.of("statusCode", statusCode));
        }

        // Читаем ответ
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(streamToRead, StandardCharsets.UTF_8))) { // !!! Тоже UTF-8
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
        }

        // Формируем headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("statusCode", statusCode);
        headers.put("statusText", connection.getResponseMessage());

        // Копируем заголовки ответа (опционально фильтруем null)
        if (connection.getHeaderFields() != null) {
            connection.getHeaderFields().forEach((key, value) -> {
                if (key != null && value != null) headers.put(key, String.join(", ", value));
            });
        }

        return new Message(responseBuilder.toString(), headers);
    }
}