package com.radovan.spring.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DeserializeConverter {
    
    private ObjectMapper objectMapper;
    
    @Autowired
    private void initialize(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode StringToJsonNode(String string) {
        try {
            return objectMapper.readTree(string); // ✅ Pretvara JSON string u JsonNode
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Ako ne može da se parsira, vraća null
        }
    }
}
