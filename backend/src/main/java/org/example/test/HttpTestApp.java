//package org.example.test;
//
//import org.example.model.core.Message;
//import org.example.service.sender.ExternalServiceClient;
//import org.example.service.sender.HttpServiceClient;
//
//import java.util.Map;
//
//public class HttpTestApp {
//    public static void main(String[] args) {
//        try {
//            ExternalServiceClient client = new HttpServiceClient();
//
//            Message message = new Message("<note><to>John</to><body>Hello XML!</body></note>");
//            message.setHeaders(Map.of(
//                    "Content-Type", "application/xml",
//                    "Accept", "application/xml",
//                    "X-HTTP-Method", "POST"
//            ));
//
//            Message response = client.send(message, "https://httpbin.org/post");
//
//            System.out.println("Response payload:");
//            System.out.println(response.getPayload());
//            System.out.println("Response headers:");
//            System.out.println(response.getHeaders());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
