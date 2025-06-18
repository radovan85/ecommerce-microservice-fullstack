package com.radovan.spring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResponseHandler {

	private ObjectMapper objectMapper;
	private RestTemplate restTemplate;

	@Autowired
	private void initialize(ObjectMapper objectMapper, RestTemplate restTemplate) {
		this.objectMapper = objectMapper;
		this.restTemplate = restTemplate;
	}

	public ResponseEntity<String> fetchAndConvertToJson(String fullUrl, HttpMethod method, HttpEntity<?> entity)
	        throws JsonMappingException, JsonProcessingException {
	    ResponseEntity<String> response = restTemplate.exchange(fullUrl, method, entity, String.class);
	    MediaType contentType = response.getHeaders().getContentType();
	    String responseBody = response.getBody();

	    if (responseBody == null || responseBody.isEmpty()) {
	        return ResponseEntity.status(response.getStatusCode()).body("{\"message\": \"No response received\"}");
	    }

	    // Ako je plain text, konvertujemo ga u JSON
	    if (contentType != null && contentType.includes(MediaType.TEXT_PLAIN)) {
	        String jsonFormattedBody = convertPlainTextToJson(responseBody);
	        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonFormattedBody);
	    }

	    // Ako je XML, konvertujemo ga u JSON
	    if (contentType != null && contentType.includes(MediaType.APPLICATION_XML)) {
	        String jsonFormattedBody = convertXmlToJson(responseBody);
	        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonFormattedBody);
	    }
	    
	    if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
	        try {
	            Object jsonObject = objectMapper.readValue(responseBody, Object.class);
	            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
	            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(prettyJson);
	        } catch (JsonProcessingException e) {
	            // fallback ako je Content-Type = JSON, ali body zapravo nije
	            String fallback = convertPlainTextToJson(responseBody);
	            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fallback);
	        }
	    }


	    // Ako je JSON, samo ga lepo formatiramo
	    Object jsonObject = objectMapper.readValue(responseBody, Object.class);
	    String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
	    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(prettyJson);
	}


	private String convertPlainTextToJson(String text) throws JsonProcessingException {
		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put("message", text); // Konvertujemo plain text u JSON strukturu
		return objectMapper.writeValueAsString(jsonMap);
	}

	private String convertXmlToJson(String xml) throws JsonProcessingException {
	    XmlMapper xmlMapper = new XmlMapper();
	    Object jsonObject = xmlMapper.readValue(xml, Object.class);
	    return objectMapper.writeValueAsString(jsonObject);
	}
}
