package dev.lilcurio.schema;

import io.apicurio.registry.json.rules.compatibility.JsonSchemaCompatibilityChecker;
import io.apicurio.registry.json.rules.validity.JsonSchemaContentValidator;
import io.apicurio.registry.rules.compatibility.CompatibilityChecker;
import io.apicurio.registry.rules.validity.ContentValidator;

public class JsonSchemaSupport implements SchemaSupport {

    private final ContentValidator validator = new JsonSchemaContentValidator();
    private final CompatibilityChecker checker = new JsonSchemaCompatibilityChecker();

    @Override
    public ContentValidator getContentValidator() {
        return validator;
    }

    @Override
    public CompatibilityChecker getCompatibilityChecker() {
        return checker;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
