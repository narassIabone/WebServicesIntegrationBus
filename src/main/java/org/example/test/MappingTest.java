package org.example.test;

import java.util.Map;
import org.example.service.MessageMapper;

public class MappingTest {
    public static void main(String[] args) {
        try {
            String originalJson = "{\"ID\":\"123\", \"body\":\"Hello\"}";

            Map<String, String> fieldMapping = Map.of(
                    "ID", "UUID",
                    "body", "content"
            );

            String transformed = MessageMapper.applyMapping(originalJson, fieldMapping);
            System.out.println(originalJson);
            System.out.println(transformed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
