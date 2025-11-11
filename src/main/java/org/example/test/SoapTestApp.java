package org.example.test;

import org.example.model.Message;
import org.example.service.ExternalServiceClient;
import org.example.service.RestServiceClient;
import org.example.service.ServiceClientFactory;

public class SoapTestApp {
    public static void main(String[] args) {
        try {
            // Выбираем клиент через фабрику
            ExternalServiceClient client = ServiceClientFactory.getClient("rest");

            // Создаем payload JSON
            String jsonPayload = "{\"title\":\"Integration Test\",\"body\":\"Hello from system\",\"userId\":1}";

            // Создаем сообщение
            Message request = new Message(jsonPayload);

            // Отправляем сообщение на REST endpoint
            Message response = client.send(request, "https://jsonplaceholder.typicode.com/posts");

            // Выводим результат
            System.out.println("Response payload: " + response.getPayload());
            System.out.println("Response headers: " + response.getHeaders());

        } catch (Exception e) {
            e.printStackTrace();
        }
        }
}
