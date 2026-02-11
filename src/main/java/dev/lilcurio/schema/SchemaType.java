package dev.lilcurio.schema;

public enum SchemaType {
    JSON("json", "application/json"),
    AVRO("avro", "application/avro+json"),
    PROTOBUF("protobuf", "application/x-protobuf");

    private final String cliName;
    private final String contentType;

    SchemaType(String cliName, String contentType) {
        this.cliName = cliName;
        this.contentType = contentType;
    }

    public String getCliName() {
        return cliName;
    }

    public String getContentType() {
        return contentType;
    }

    public static SchemaType fromCliName(String name) {
        for (SchemaType t : values()) {
            if (t.cliName.equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException(
                "Unsupported schema type: " + name + ". Supported: json");
    }
}
