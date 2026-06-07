package org.example.engine.processor.impl;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.example.engine.exception.FatalException;
import org.example.engine.exception.RetryableException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.entity.Message;
import org.example.model.entity.NodeConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import javax.xml.stream.XMLInputFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Универсальный процессор для выполнения внешних API-вызовов (REST, SOAP, HTTP RAW).
 * Поддерживает динамическую подстановку параметров из Header и Context сообщения через {key}.
 * Автоматически конвертирует XML-ответы в JSON для обеспечения единообразия данных в шине.
 *
 * --- ОБЩИЕ ПАРАМЕТРЫ (NodeConfig params) ---
 * - url: [Required] URL адрес (поддерживает плейсхолдеры {key})
 * - method: GET, POST, PUT, DELETE (дефолт: POST)
 * - timeout: Таймаут в мс (дефолт: 5000)
 * - protocol: REST, SOAP, RAW (дефолт: REST)
 *
 * --- УПРАВЛЕНИЕ ДАННЫМИ (PAYLOAD) ---
 * - target_field: Поле или список полей для отправки во внешнюю систему.
 * * Если указано одно поле (напр. "passport") — извлекается только его значение.
 * * Если указано несколько (напр. "fio,birthday") — формируется новый JSON-объект.
 * * Если пусто — отправляется весь текущий payload сообщения.
 *
 * --- УПРАВЛЕНИЕ ЗАГОЛОВКАМИ (HEADERS) ---
 * - custom_headers: Дополнительные заголовки в формате "Key1=Value1,Key2=Value2".
 * * Поддерживает плейсхолдеры (напр. "X-Request-ID={message_id},Authorization={my_token}").
 * - content_type: Кастомный заголовок (для протокола RAW, напр. text/csv)
 *
 * --- ТИПЫ АВТОРИЗАЦИИ (auth_type) ---
 * - auth_type = NONE: Без авторизации (дефолт)
 * - auth_type = BEARER: Использует 'auth_token' (напр. {jwt_token})
 * - auth_type = API_KEY: Использует 'api_key_name' (дефолт: X-API-Key) и 'api_key_value'
 * - auth_type = BASIC: Использует 'auth_user' и 'auth_password'
 *
 * --- ПРОТОКОЛ SOAP ---
 * (Автоматически оборачивает payload в Envelope и конвертирует XML-ответ в JSON)
 * - soap_root_element: Имя корневого тега внутри SOAP Body (напр. "getLoanRequest")
 * - soap_namespace: URI пространства имен (напр. "http://example.org/loans")
 * - soap_prefix: Префикс для пространства имен (напр. "loan")
 * - soap_action: Значение заголовка SOAPAction
 * - is_namespace_aware: [boolean] Учет пространств имен при парсинге ответа (дефолт: false).
 * * При false — автоматически удаляет префиксы из ключей JSON (напр. "ns2:status" -> "status").
 *
 * --- СТРАТЕГИИ СОХРАНЕНИЯ ОТВЕТА (response_strategy) ---
 * - OVERRIDE: Ответ полностью заменяет текущий payload (дефолт)
 * - MERGE_FULL: Создает структуру { "request": original, "response": api_result }
 * - MERGE_SELECTIVE: Добавляет поля из ответа в оригинал согласно 'response_mapping'
 * - response_mapping: Маппинг полей "куда=откуда" через запятую (напр. contract=Body.getResponse.id)
 *
 * --- ОБРАБОТКА ОШИБОК ---
 * - RetryableException: Генерируется при таймаутах (SocketTimeout) и ошибках сервера (5xx).
 * * Позволяет RouteExecutor выполнить повторную попытку через retry-топик.
 * - FatalException: Генерируется при ошибках клиента (4xx), авторизации (401/403) и ошибках трансформации данных.
 * * Сообщение немедленно перенаправляется в очередь ошибок (messages.error).
 *
 * --- РЕЗУЛЬТАТ РАБОТЫ ---
 * - В Context сообщения записывается "{nodeId}.http_status" (код ответа API).
 * - Ответы XML (SOAP) автоматически приводятся к формату JSON.
 */
@Slf4j
@Component("API_CALL")
public class ApiCallProcessor implements NodeProcessor {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public ApiCallProcessor(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        Map<String, String> params = config.getConfig();
        int timeoutMs = Integer.parseInt(params.getOrDefault("timeout", "5000"));
        WebClient webClient = createCustomWebClient(timeoutMs);

        String finalUrl = resolvePlaceholders(params.get("url"), message);
        String method = params.getOrDefault("method", "POST").toUpperCase();
        String protocol = params.getOrDefault("protocol", "REST").toUpperCase();
        String targetFields = params.get("target_field");
        String filteredPayload = filterPayload(message.getPayload(), targetFields);

        // Логируем исходящий запрос
        log.info("[Node 'API_CALL' ({})] Вызов внешнего сервиса [{}]: {} {}",
                config.getName(), protocol, method, finalUrl);

        try {
            var responseEntity = webClient.method(HttpMethod.valueOf(method))
                    .uri(java.net.URI.create(finalUrl))
                    .headers(headers -> {
                        setupStandardHeaders(headers, protocol, params);
                        setupSecurityHeaders(headers, params, message);
                        setupCustomHeaders(headers, params, message);
                    })
                    .bodyValue(preparePayload(protocol, filteredPayload, params))
                    .retrieve()
                    .toEntity(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (responseEntity != null) {
                String statusCode = String.valueOf(responseEntity.getStatusCode().value());
                String responseBody = responseEntity.getBody();

                // Сохраняем статус в контекст
                message.getContext().put(config.getId() + ".http_status", statusCode);

                // КЛЮЧЕВОЙ ЛОГ: Ответ от API
                log.info("[Node 'API_CALL' ({})] Получен ответ (Status: {}). Body: {}",
                        config.getName(), statusCode, responseBody);

                handleResponseData(message, responseBody, params, config.getName());
            }

            return ProcessorResult.builder()
                    .envelope(ProcessorResult.OutboundEnvelope.builder()
                            .message(message)
                            .build())
                    .build();

        } catch (Exception e) {
            handleException(e, config.getId());
            return null; // unreachable, handleException бросает runtime exception
        }
    }

    private void handleResponseData(Message message, String responseBody, Map<String, String> params, String nodeName) {
        String strategy = params.getOrDefault("response_strategy", "OVERRIDE").toUpperCase();
        String protocol = params.getOrDefault("protocol", "REST").toUpperCase();
        String processedResponse = responseBody;

        if ("SOAP".equals(protocol) || isXml(responseBody)) {
            try {
                processedResponse = convertXmlToJson(responseBody, params);
                log.debug("[Node 'API_CALL' ({})] XML успешно сконвертирован в JSON", nodeName);
            } catch (Exception e) {
                log.error("[Error] Ошибка конвертации XML для ноды {}: {}", nodeName, e.getMessage());
            }
        }

        log.debug("[Node 'API_CALL' ({})] Применение стратегии сохранения: {}", nodeName, strategy);

        switch (strategy) {
            case "MERGE_FULL" -> message.setPayload(mergeFull(message.getPayload(), processedResponse));
            case "MERGE_SELECTIVE" -> {
                String mapping = params.get("response_mapping");
                message.setPayload(mergeSelective(message.getPayload(), processedResponse, mapping));
            }
            default -> message.setPayload(processedResponse);
        }
    }

    private void handleException(Exception e, int nodeId) {
        String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
        log.error("[Error] Сбой внешнего вызова в ноде {}: {}", nodeId, errorMsg);

        if (e instanceof java.net.SocketTimeoutException || e.getCause() instanceof java.net.ConnectException || errorMsg.contains("Timeout")) {
            throw new RetryableException("Сетевой таймаут: " + errorMsg);
        }
        if (errorMsg.contains("500") || errorMsg.contains("502") || errorMsg.contains("503") || errorMsg.contains("504")) {
            throw new RetryableException("Временная ошибка сервера (5xx): " + errorMsg);
        }
        if (errorMsg.contains("401") || errorMsg.contains("403")) {
            throw new FatalException("Ошибка доступа (401/403): " + errorMsg);
        }
        throw new FatalException("Критическая ошибка API: " + errorMsg);
    }

    private String filterPayload(String originalPayload, String targetFields) {
        if (targetFields == null || targetFields.isBlank()) {
            return originalPayload;
        }

        try {
            JsonNode root = objectMapper.readTree(originalPayload);
            String[] fields = targetFields.split(",");

            if (fields.length == 1) {
                JsonNode singleNode = root.get(fields[0].trim());
                return singleNode != null ? objectMapper.writeValueAsString(singleNode) : "{}";
            } else {
                ObjectNode filteredNode = objectMapper.createObjectNode();
                for (String field : fields) {
                    String fieldName = field.trim();
                    if (root.has(fieldName)) {
                        filteredNode.set(fieldName, root.get(fieldName));
                    }
                }
                return objectMapper.writeValueAsString(filteredNode);
            }
        } catch (Exception e) {
            System.err.println("[API_CALL] Payload filtering failed: " + e.getMessage());
            return originalPayload;
        }
    }

    private void setupCustomHeaders(HttpHeaders headers, Map<String, String> params, Message message) {
        String customHeadersStr = params.get("custom_headers");
        if (customHeadersStr != null && !customHeadersStr.isBlank()) {
            String[] pairs = customHeadersStr.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String headerName = kv[0].trim();
                    String headerValue = resolvePlaceholders(kv[1].trim(), message);
                    headers.set(headerName, headerValue);
                }
            }
        }
    }

    private boolean isXml(String body) {
        return body != null && (body.trim().startsWith("<") || body.trim().startsWith("<?xml"));
    }

    private String mergeFull(String original, String response) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.set("request", parseToJson(original));
            root.set("response", parseToJson(response));
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"Merge failed\"}";
        }
    }

    private String mergeSelective(String original, String response, String mapping) {
        try {
            JsonNode originalJson = parseToJson(original);
            JsonNode responseJson = parseToJson(response);
            if (!(originalJson instanceof ObjectNode)) return response;

            ObjectNode resultNode = (ObjectNode) originalJson;
            if (mapping != null && !mapping.isEmpty()) {
                for (String pair : mapping.split(",")) {
                    String[] parts = pair.split("=");
                    if (parts.length == 2) {
                        JsonNode valueNode = findNodeByPath(responseJson, parts[1].trim());
                        if (valueNode != null) resultNode.set(parts[0].trim(), valueNode);
                    }
                }
            }
            return objectMapper.writeValueAsString(resultNode);
        } catch (Exception e) { return original; }
    }

    private JsonNode findNodeByPath(JsonNode root, String path) {
        JsonNode current = root;
        for (String step : path.split("\\.")) {
            if (current == null || current.isMissingNode()) return null;

            if (current.isArray()) {
                try {
                    int index = Integer.parseInt(step);
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                current = current.get(step);
            }
        }
        return current;
    }

    private JsonNode parseToJson(String content) {
        try { return objectMapper.readTree(content); }
        catch (Exception e) { return objectMapper.getNodeFactory().textNode(content); }
    }

    private String convertXmlToJson(String xml, Map<String, String> params) {
        try {
            boolean isNamespaceAware = Boolean.parseBoolean(params.getOrDefault("is_namespace_aware", "false"));
            XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, isNamespaceAware);

            XmlMapper xmlMapper = new XmlMapper(inputFactory);
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode node = xmlMapper.readValue(xml, JsonNode.class);
            String json = objectMapper.writeValueAsString(node);

            if (!isNamespaceAware) {
                json = json.replaceAll("\"[a-zA-Z0-9]+:([^\"]+)\":", "\"$1\":");
            }

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private WebClient createCustomWebClient(int timeoutMs) {
        org.springframework.web.util.DefaultUriBuilderFactory factory = new org.springframework.web.util.DefaultUriBuilderFactory();
        factory.setEncodingMode(org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode.NONE);
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));
        return webClientBuilder.uriBuilderFactory(factory).clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    private String resolvePlaceholders(String template, Message message) {
        if (template == null) return null;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object val = message.getHeaders().getOrDefault(key, message.getContext().get(key));
            String value = (val != null) ? String.valueOf(val) : matcher.group(0);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private void setupSecurityHeaders(HttpHeaders headers, Map<String, String> params, Message message) {
        String authType = params.getOrDefault("auth_type", "NONE").toUpperCase();
        switch (authType) {
            case "BEARER" -> headers.setBearerAuth(resolvePlaceholders(params.get("auth_token"), message));
            case "API_KEY" -> headers.set(params.getOrDefault("api_key_name", "X-API-Key"), resolvePlaceholders(params.get("api_key_value"), message));
            case "BASIC" -> headers.setBasicAuth(resolvePlaceholders(params.get("auth_user"), message), resolvePlaceholders(params.get("auth_password"), message));
        }
    }

    private void setupStandardHeaders(HttpHeaders headers, String protocol, Map<String, String> params) {
        if ("SOAP".equals(protocol)) headers.setContentType(MediaType.TEXT_XML);
        else if ("RAW".equals(protocol)) headers.set(HttpHeaders.CONTENT_TYPE, params.getOrDefault("content_type", "text/plain"));
        else headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String preparePayload(String protocol, String originalPayload, Map<String, String> params) {
        if ("SOAP".equals(protocol)) {
            try {
                String root = params.getOrDefault("soap_root_element", "payload");
                String ns = params.get("soap_namespace");
                String prefix = params.get("soap_prefix");

                String xmlBody = org.example.service.transformer.MessageTransformer.jsonToXml(
                        originalPayload, root, ns, prefix
                );

                return String.format(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                "<soapenv:Body>%s</soapenv:Body></soapenv:Envelope>",
                        xmlBody
                );
            } catch (Exception e) {
                throw new FatalException("Ошибка трансформации данных для SOAP: " + e.getMessage());
            }
        }
        return originalPayload;
    }
}