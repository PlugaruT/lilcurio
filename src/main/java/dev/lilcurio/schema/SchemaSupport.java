package dev.lilcurio.schema;

import io.apicurio.registry.rules.compatibility.CompatibilityChecker;
import io.apicurio.registry.rules.validity.ContentValidator;

public interface SchemaSupport {
    ContentValidator getContentValidator();
    CompatibilityChecker getCompatibilityChecker();
    String getContentType();
}
