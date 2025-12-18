package org.example.engine.processor.impl;

import org.example.engine.processor.NodeProcessor;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.example.service.sender.RestServiceClient;

// NodeType = REST_CALL
public class RestCallNodeProcessor implements NodeProcessor {

    private final RestServiceClient restClient = new RestServiceClient();

    @Override
    public Message process(Message message, NodeConfig nodeConfig) throws Exception {

        String url = (String) nodeConfig.getConfig().get("url");
        String formatStr = (String) nodeConfig.getConfig().getOrDefault("format", "JSON");

        RestServiceClient.Format format =
                RestServiceClient.Format.valueOf(formatStr.toUpperCase());

        if (url == null) {
            throw new IllegalArgumentException("REST_CALL node requires 'url' in config");
        }

        return restClient.send(message, url, format);
    }
}