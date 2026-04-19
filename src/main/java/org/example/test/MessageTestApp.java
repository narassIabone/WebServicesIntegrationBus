//package org.example.test;
//
//import org.example.model.core.Message;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.cfg.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class MessageTestApp {
//
//    public static void main(String[] args) {
//        // Инициализация Hibernate (убедитесь, что hibernate.cfg.xml настроен)
//        try (SessionFactory sessionFactory = new Configuration()
//                .configure() // загружает hibernate.cfg.xml
//                .buildSessionFactory();
//             Session session = sessionFactory.openSession()) {
//
//            // --- 1. Подготовка данных ---
//            // Исправлено: Тип значения Object, так как в классе Message теперь Map<String, Object>
//            Map<String, Object> headers = new HashMap<>();
//            headers.put("source", "frontend");
//            headers.put("client", "client1");
//            headers.put("retryCount", 1); // Теперь можно класть и числа
//
//            // --- 2. Сохранение сообщения ---
//            System.out.println("--- Saving Message ---");
//            session.beginTransaction();
//
//            Message message = new Message("Hello World 2");
//            message.setHeaders(headers);
//
//            // Исправлено: getTimestamp() -> getCreatedAt()
//            System.out.println("ID: " + message.getId());
//            System.out.println("Payload: " + message.getPayload());
//            System.out.println("Headers: " + message.getHeaders());
//            System.out.println("Created At: " + message.getCreatedAt());
//            System.out.println("Status: " + message.getStatus());
//
//            session.save(message);
//            session.getTransaction().commit();
//
//            // --- 3. Получение сообщения ---
//            System.out.println("\n--- Retrieving Message ---");
//            session.beginTransaction();
//
//            // Hibernate загрузит сообщение по ID
//            Message retrievedMessage = session.get(Message.class, message.getId());
//
//            if (retrievedMessage != null) {
//                System.out.println("ID: " + retrievedMessage.getId());
//                System.out.println("Payload: " + retrievedMessage.getPayload());
//                System.out.println("Headers: " + retrievedMessage.getHeaders());
//                // Исправлено: getTimestamp() -> getCreatedAt()
//                System.out.println("Created At: " + retrievedMessage.getCreatedAt());
//                System.out.println("Status: " + retrievedMessage.getStatus());
//            } else {
//                System.out.println("Message not found!");
//            }
//
//            session.getTransaction().commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}