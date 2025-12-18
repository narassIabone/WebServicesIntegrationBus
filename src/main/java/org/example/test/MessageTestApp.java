package org.example.test;

import org.example.model.core.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.HashMap;
import java.util.Map;

public class MessageTestApp {

    public static void main(String[] args) {
        try (SessionFactory sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            // Создаем headers
            Map<String, String> headers = new HashMap<>();
            headers.put("source", "frontend");
            headers.put("client", "client1");

            // Сохранение сообщения
            session.beginTransaction();
            Message message = new Message("Hello World 2");
            message.setHeaders(headers); // устанавливаем headers

            System.out.println("ID: " + message.getId());
            System.out.println("Payload: " + message.getPayload());
            System.out.println("Headers: " + message.getHeaders());
            System.out.println("Timestamp: " + message.getTimestamp());
            System.out.println("Status: " + message.getStatus());

            session.save(message);
            session.getTransaction().commit();

            // Получение сообщения
            session.beginTransaction();
            Message retrievedMessage = session.get(Message.class, message.getId());

            System.out.println("Retrieved message:");
            System.out.println("ID: " + retrievedMessage.getId());
            System.out.println("Payload: " + retrievedMessage.getPayload());
            System.out.println("Headers: " + retrievedMessage.getHeaders());
            System.out.println("Timestamp: " + retrievedMessage.getTimestamp());
            System.out.println("Status: " + retrievedMessage.getStatus());

            session.getTransaction().commit();
        }
    }
}
