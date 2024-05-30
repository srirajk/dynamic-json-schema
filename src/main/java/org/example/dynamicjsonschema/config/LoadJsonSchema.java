package org.example.dynamicjsonschema.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoadJsonSchema {

    private Map<String, JsonSchema> schemas = new HashMap<>();

    @Value("classpath:/schemas/*.json")
    private Resource[] schemaResources;

    @PostConstruct
    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        for (Resource resource : schemaResources) {
            try {
                String fileName = resource.getFilename();
                JsonSchema schema = schemaFactory.getSchema(objectMapper.readTree(resource.getInputStream()));
                String schemaId = fileName.substring(0, fileName.lastIndexOf('.'));
                if (fileName.equals("BaseCase.json")) {
                    schemas.put(schemaId, schema);
                } else if (schema.getSchemaNode().has("allOf")) {
                    boolean extendsBaseCase = false;
                    for (JsonNode refNode : schema.getSchemaNode().get("allOf")) {
                        if (refNode.has("$ref") && refNode.get("$ref").asText().contains("BaseCase.json")) {
                            extendsBaseCase = true;
                            break;
                        }
                    }
                    if (extendsBaseCase) {
                        schemas.put(schemaId, schema);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading JSON schema", e);
            }
        }
    }

    public JsonSchema getSchema(String schemaId) {
        return schemas.get(schemaId);
    }
}