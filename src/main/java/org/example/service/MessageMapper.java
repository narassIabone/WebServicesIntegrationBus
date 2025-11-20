package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public class MessageMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String applyMapping(String payload, Map<String, String> fieldMapping) throws Exception {
        JsonNode original = mapper.readTree(payload);
        ObjectNode transformed = mapper.createObjectNode();

        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            JsonNode value = original.get(from);
            if (value != null) {
                transformed.set(to, value);
            }
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformed);
    }
}
