package dev.lilcurio.io;

import io.apicurio.registry.content.TypedContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchemaFileReader {

    public static TypedContent read(Path filePath, String contentType) throws IOException {
        String content = Files.readString(filePath);
        return TypedContent.create(content, contentType);
    }
}
