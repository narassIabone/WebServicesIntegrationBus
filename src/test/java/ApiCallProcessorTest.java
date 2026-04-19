import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.example.engine.processor.ProcessorResult;
import org.example.engine.processor.impl.ApiCallProcessor;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiCallProcessorTest {

    private MockWebServer mockWebServer;
    private ApiCallProcessor processor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        processor = new ApiCallProcessor(WebClient.builder(), objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // --- ГРУППА 1: REST (JSON) ---
    @Nested
    @DisplayName("REST API Tests")
    class RestTests {

        @Test
        @DisplayName("REST: Full Integration with Merge and Auth")
        void testRest_FullIntegration() throws Exception {
            // 1. Готовим ответ от API
            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"api_status\": \"active\", \"code\": 123}")
                    .addHeader("Content-Type", "application/json")
                    .setResponseCode(200));

            // 2. Создаем сообщение с данными для подстановки
            Message message = new Message();
            message.setPayload("{\"user\": \"admin\"}");
            message.setHeaders(new HashMap<>(Map.of("userId", "777", "token", "secret_jwt")));

            // 3. Формируем URL вручную, чтобы MockWebServer не закодировал скобки раньше времени
            String baseUrl = mockWebServer.url("").toString();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            NodeConfig config = new NodeConfig();
            config.setConfig(Map.of(
                    "url", baseUrl + "/users/{userId}", // Чистая строка для resolvePlaceholders
                    "method", "POST",
                    "auth_type", "BEARER",
                    "auth_token", "{token}",
                    "response_strategy", "MERGE_FULL"
            ));

            // 4. Запускаем процессор
            ProcessorResult result = processor.process(message, config);
            Message resultMsg = result.getEnvelopes().get(0).getMessage();
            RecordedRequest recordedRequest = mockWebServer.takeRequest();

            // --- ПОЛНОЕ ЛОГИРОВАНИЕ (ВОЗВРАЩЕНО) ---
            System.out.println("\n=== [TEST: REST + MERGE_FULL] ===");
            System.out.println("BEFORE (Original Payload):");
            printJson(message.getPayload());

            System.out.println("\n>> OUTGOING REQUEST TO API:");
            System.out.println("URL (Resolved): " + recordedRequest.getPath());
            System.out.println("Method:         " + recordedRequest.getMethod());
            System.out.println("Auth Header:    " + recordedRequest.getHeader("Authorization"));

            System.out.println("\n<< FINAL MESSAGE IN ENGINE:");
            System.out.println("HTTP Status:    " + resultMsg.getHeaders().get("http_status"));
            System.out.println("Final Payload:");
            printJson(resultMsg.getPayload());
            System.out.println("===================================\n");

            // --- ПРОВЕРКИ ---
            assertEquals("/users/777", recordedRequest.getPath(), "Плейсхолдер {userId} должен быть заменен на 777");
            assertEquals("Bearer secret_jwt", recordedRequest.getHeader("Authorization"), "Токен должен быть подставлен");
            assertEquals("200", resultMsg.getHeaders().get("http_status"), "HTTP статус должен быть сохранен");
            assertTrue(resultMsg.getPayload().contains("\"api_status\":\"active\""), "Ответ API должен быть в payload");
        }

        @Test
        @DisplayName("REST: Strategy Override")
        void testRest_Override() throws Exception {
            String apiResponse = "{\"new_data\": \"replacement\"}";
            mockWebServer.enqueue(new MockResponse().setBody(apiResponse).setResponseCode(200));

            Message message = new Message();
            String originalPayload = "{\"old_data\": \"delete_me\"}";
            message.setPayload(originalPayload);

            NodeConfig config = new NodeConfig();
            config.setConfig(Map.of(
                    "url", mockWebServer.url("/override").toString(),
                    "response_strategy", "OVERRIDE"
            ));

            ProcessorResult result = processor.process(message, config);
            Message resultMsg = result.getEnvelopes().get(0).getMessage();

            System.out.println("\n=== [TEST: STRATEGY OVERRIDE] ===");
            System.out.println("BEFORE (Original):");
            printJson(originalPayload);
            System.out.println("\nAFTER (Final):");
            System.out.println("HTTP Status: " + resultMsg.getHeaders().get("http_status"));
            printJson(resultMsg.getPayload());
            System.out.println("=================================\n");


            assertEquals("200", resultMsg.getHeaders().get("http_status"));
            assertFalse(resultMsg.getPayload().contains("old_data"));
        }
    }

    // --- ГРУППА 2: SOAP (XML) ---
    @Nested
    @DisplayName("SOAP XML Tests")
    class SoapTests {

        @Test
        @DisplayName("SOAP: XML to JSON and Selective Merge")
        void testSoap_MergeSelective() throws Exception {
            String xmlResponse =
                    "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "  <S:Body><GetBalanceResponse><AccountInfo><Amount>1500.50</Amount><Currency>USD</Currency></AccountInfo></GetBalanceResponse></S:Body>" +
                            "</S:Envelope>";

            mockWebServer.enqueue(new MockResponse().setBody(xmlResponse).setResponseCode(200));

            Message message = new Message();
            String original = "{\"clientId\": \"user_01\"}";
            message.setPayload(original);

            NodeConfig config = new NodeConfig();
            config.setConfig(Map.of(
                    "url", mockWebServer.url("/soap-selective").toString(),
                    "protocol", "SOAP",
                    "response_strategy", "MERGE_SELECTIVE",
                    "response_mapping", "balance=S:Body.GetBalanceResponse.AccountInfo.Amount, valuta=S:Body.GetBalanceResponse.AccountInfo.Currency"
            ));

            ProcessorResult result = processor.process(message, config);
            Message resultMsg = result.getEnvelopes().get(0).getMessage();

            System.out.println("\n=== [TEST: SOAP + MERGE_SELECTIVE] ===");
            System.out.println("BEFORE (Original JSON):");
            printJson(original);
            System.out.println("\nFROM API (Raw SOAP XML):");
            System.out.println(xmlResponse);
            System.out.println("\nAFTER (Merged Payload):");
            System.out.println("HTTP Status: " + resultMsg.getHeaders().get("http_status"));
            printJson(resultMsg.getPayload());
            System.out.println("======================================\n");

            assertEquals("200", resultMsg.getHeaders().get("http_status"));
            assertTrue(resultMsg.getPayload().contains("\"balance\":\"1500.50\""));
        }
    }

    // --- ГРУППА 3: HTTP RAW ---
    @Nested
    @DisplayName("HTTP RAW Tests")
    class RawTests {

        @Test
        @DisplayName("RAW: Custom Content-Type and 202 Status")
        void testRaw_CsvUpload() throws Exception {
            String mockResponse = "ACCEPTED_RAW_DATA";
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(202));

            Message message = new Message();
            String csvData = "id;value\n1;100";
            message.setPayload(csvData);

            NodeConfig config = new NodeConfig();
            config.setConfig(Map.of(
                    "url", mockWebServer.url("/upload").toString(),
                    "protocol", "RAW",
                    "content_type", "text/csv",
                    "response_strategy", "OVERRIDE"
            ));

            ProcessorResult result = processor.process(message, config);
            Message resultMsg = result.getEnvelopes().get(0).getMessage();
            RecordedRequest recordedRequest = mockWebServer.takeRequest();

            System.out.println("\n=== [TEST: PROTOCOL RAW] ===");
            System.out.println(">> OUTGOING CONTENT-TYPE: " + recordedRequest.getHeader("Content-Type"));
            System.out.println("\n<< FINAL MESSAGE STATE:");
            System.out.println("HTTP Status: " + resultMsg.getHeaders().get("http_status"));
            System.out.println("Payload: " + resultMsg.getPayload());
            System.out.println("============================\n");

            assertEquals("202", resultMsg.getHeaders().get("http_status"));
            assertEquals("text/csv", recordedRequest.getHeader("Content-Type"));
            assertEquals(mockResponse, resultMsg.getPayload());
        }
    }

    private void printJson(String payload) {
        try {
            Object json = objectMapper.readValue(payload, Object.class);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        } catch (Exception e) {
            System.out.println(payload);
        }
    }
}