package dev.lilcurio.schema;

import java.util.EnumMap;
import java.util.Map;

public class SchemaTypeRegistry {

    private static final Map<SchemaType, SchemaSupport> REGISTRY = new EnumMap<>(SchemaType.class);

    static {
        REGISTRY.put(SchemaType.JSON, new JsonSchemaSupport());
    }

    public static SchemaSupport get(SchemaType type) {
        SchemaSupport support = REGISTRY.get(type);
        if (support == null) {
            throw new IllegalArgumentException(
                    "No support registered for schema type: " + type.getCliName());
        }
        return support;
    }
}
