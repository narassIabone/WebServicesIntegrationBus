package org.example.service.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class MessageTransformer {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();

    static {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
    }

    public static String jsonToXml(String jsonPayload, String rootElement, String namespace, String prefix) throws JsonProcessingException {
        Object obj = jsonMapper.readValue(jsonPayload, Object.class);

        if (rootElement == null || rootElement.isEmpty()) {
            rootElement = "payload";
        }

        String xml = xmlMapper.writer().withRootName(rootElement).writeValueAsString(obj);

        if (namespace == null || namespace.isEmpty()) {
            return xml;
        }

        String nsAttr = (prefix == null || prefix.isEmpty())
                ? String.format(" xmlns=\"%s\"", namespace)
                : String.format(" xmlns:%s=\"%s\"", prefix, namespace);

        String taggedRoot = (prefix == null || prefix.isEmpty()) ? rootElement : prefix + ":" + rootElement;

        return xml.replace("<" + rootElement + ">", "<" + taggedRoot + nsAttr + ">")
                .replace("</" + rootElement + ">", "</" + taggedRoot + ">");
    }

    public static String xmlToJson(String xmlPayload) throws JsonProcessingException {
        Object obj = xmlMapper.readValue(xmlPayload, Object.class);
        return jsonMapper.writeValueAsString(obj);
    }
}
