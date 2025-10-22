package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class MessageTransformer {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();

    public static String jsonToXml(String jsonPayload, String rootElement) throws JsonProcessingException {
        Object obj = jsonMapper.readValue(jsonPayload, Object.class);

        if (rootElement == null || rootElement.isEmpty()) {
            rootElement = "payload";
        }
        return xmlMapper.writer().withRootName(rootElement).writeValueAsString(obj);
    }

    public static String xmlToJson(String xmlPayload) throws JsonProcessingException {
        Object obj = xmlMapper.readValue(xmlPayload, Object.class);
        return jsonMapper.writeValueAsString(obj);
    }
}
