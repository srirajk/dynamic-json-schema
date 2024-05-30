package org.example.dynamicjsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import org.example.dynamicjsonschema.config.LoadJsonSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/json")
public class DynamicJsonController {

    private LoadJsonSchema loadJsonSchema;

    public DynamicJsonController(LoadJsonSchema loadJsonSchema) {
        this.loadJsonSchema = loadJsonSchema;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateJson(@RequestBody JsonNode jsonObject,
                                          @RequestParam(name = "schemaId") String schemaId) {
        JsonSchema schema = loadJsonSchema.getSchema(schemaId);

        if (Objects.isNull(schema)) {
            return ResponseEntity.badRequest().body("Invalid schemaId: " + schemaId);
        }

        Set<ValidationMessage> errors = schema.validate(jsonObject);

        if (errors.isEmpty()) {
            return ResponseEntity.ok().body("JSON is valid");
        } else {
            return ResponseEntity.badRequest().body(errors);
        }
    }
}