package org.example.service.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public String applyMapping(String currentPayload, Map<String, String> mapping) throws Exception {
        if (mapping == null || mapping.isEmpty()) {
            return currentPayload;
        }

        JsonNode root = mapper.readTree(currentPayload);
        ObjectNode resultNode = mapper.createObjectNode();

        root.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();

            if (mapping.containsKey(fieldName)) {
                String targetKey = mapping.get(fieldName);

                if (targetKey != null && !targetKey.equalsIgnoreCase("null")) {
                    resultNode.set(targetKey, fieldValue);
                }
            } else {
                resultNode.set(fieldName, fieldValue);
            }
        });

        return mapper.writeValueAsString(resultNode);
    }
}
