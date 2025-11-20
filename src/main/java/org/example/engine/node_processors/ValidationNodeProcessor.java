package org.example.engine.node_processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.engine.NodeProcessor;
import org.example.model.Message;
import org.example.model.route.NodeConfig;

import java.util.List;
import java.util.Map;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class ValidationNodeProcessor implements NodeProcessor {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Message process(Message message, NodeConfig config) throws Exception {

        Map<String, Object> rules = config.getConfig();
        if (rules == null || rules.isEmpty()) {
            return message; // ничего не проверять
        }

        JsonNode json = mapper.readTree(message.getPayload());

        // 1. Проверка обязательных полей
        if (rules.containsKey("requiredFields")) {
            List<String> requiredFields = (List<String>) rules.get("requiredFields");
            for (String field : requiredFields) {
                if (!json.has(field)) {
                    throw new ValidationException("Missing required field: " + field);
                }
            }
        }

        // 2. Проверка regex
        if (rules.containsKey("regex")) {
            Map<String, String> regexRules = (Map<String, String>) rules.get("regex");

            for (Map.Entry<String, String> entry : regexRules.entrySet()) {
                String field = entry.getKey();
                String pattern = entry.getValue();

                if (json.has(field)) {
                    String value = json.get(field).asText();
                    if (!value.matches(pattern)) {
                        throw new ValidationException(
                                "Field '" + field + "' does not match regex " + pattern
                        );
                    }
                }
            }
        }

        // 3. Простые числовые проверки
        if (rules.containsKey("numeric")) {
            Map<String, Map<String, Number>> numericRules =
                    (Map<String, Map<String, Number>>) rules.get("numeric");

            for (String field : numericRules.keySet()) {
                if (!json.has(field)) continue;

                double value = json.get(field).asDouble();
                Map<String, Number> constraints = numericRules.get(field);

                if (constraints.containsKey("min") && value < constraints.get("min").doubleValue()) {
                    throw new ValidationException("Field '" + field + "' < min");
                }
                if (constraints.containsKey("max") && value > constraints.get("max").doubleValue()) {
                    throw new ValidationException("Field '" + field + "' > max");
                }
            }
        }

        // 4. Проверка даты
        Map<String, String> dateFormats = (Map<String, String>) rules.get("date");
        if (dateFormats != null) {
            for (var entry : dateFormats.entrySet()) {
                String field = entry.getKey();
                String pattern = entry.getValue();

                if (!json.has(field)) {
                    throw new ValidationException("Missing date field: " + field);
                }

                String value = json.get(field).asText();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    LocalDate.parse(value, formatter); // или LocalDateTime.parse
                } catch (Exception ex) {
                    throw new ValidationException("Invalid date for field '" + field + "': " + value);
                }
            }
        }

        return message; // всё ок — пропускаем дальше
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
