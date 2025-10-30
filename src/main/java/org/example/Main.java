package org.example;

import org.example.model.Message;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        try (SessionFactory sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            // Сохранение сообщения
            session.beginTransaction();
            Message message = new Message("Hello World 2");
            System.out.println("ID: " + message.getId());
            System.out.println("Payload: " + message.getPayload());
            System.out.println("Timestamp: " + message.getTimestamp());
            System.out.println("Status: " + message.getStatus());
            session.save(message);
            session.getTransaction().commit();

            // Получение сообщения
            session.beginTransaction();
            Message retrievedMessage = session.get(Message.class, message.getId());
            System.out.println("Retrieved message: " + retrievedMessage);
            session.getTransaction().commit();
        }
    }
}
