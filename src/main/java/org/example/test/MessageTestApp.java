package org.example.test;

import org.example.model.Message;
import org.example.service.MessageTransformer;

public class MessageTestApp{

    public static void main(String[] args) {
        try {
            // Создаем headers
//            Map<String, String> headers = new HashMap<>();
//            headers.put("source", "frontend");
//            headers.put("client", "client1");

            // Создаем payload в формате JSON
            String jsonPayload = "{\"user\":\"Alice\",\"action\":\"login\"}";

            // Создаем объект Message
//            Message message = new Message(jsonPayload, headers);
            Message message = new Message(jsonPayload);
            System.out.println("Сообщение пришло в JSON:");
            System.out.println("ID: " + message.getId());
            System.out.println("JSON Payload: " + message.getPayload());
            //System.out.println("Headers: " + message.getHeaders());
            System.out.println("Timestamp: " + message.getTimestamp());
            System.out.println("Status: " + message.getStatus());

            System.out.println("Сообщение трансформировано в XML:");
            String xmlPayload = MessageTransformer.jsonToXml(message.getPayload(), "sad");
            message.setPayload(xmlPayload);
            System.out.println("XML Payload: " + message.getPayload());

            System.out.println("Сообщение трансформировано обратно в JSON:");
            jsonPayload = MessageTransformer.xmlToJson(message.getPayload());
            message.setPayload(jsonPayload);
            System.out.println("JSON Payload: " + message.getPayload());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
