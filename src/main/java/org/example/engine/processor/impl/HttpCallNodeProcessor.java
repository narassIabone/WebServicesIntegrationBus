package org.example.engine.processor.impl;

import org.example.engine.processor.NodeProcessor;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.example.service.sender.RestServiceClient;

//public class HttpCallNodeProcessor implements NodeProcessor {

//    private final RestServiceClient client = new RestServiceClient();

//    @Override
//    public Message process(Message message, NodeConfig config) throws Exception {
//
//        // Достаём параметры узла
//        String url = (String) config.getConfig().get("url");
//        String formatStr = (String) config.getConfig().getOrDefault("format", "JSON");
//
//        RestServiceClient.Format format =
//                formatStr.equalsIgnoreCase("XML")
//                        ? RestServiceClient.Format.XML
//                        : RestServiceClient.Format.JSON;
//
//        if (url == null) {
//            throw new IllegalArgumentException("HTTP_CALL node missing 'url' in config");
//        }
//
//        return client.send(message, url, format);
//    }
//}