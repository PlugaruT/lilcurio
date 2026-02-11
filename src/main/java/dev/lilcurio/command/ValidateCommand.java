package dev.lilcurio.command;

import dev.lilcurio.io.SchemaFileReader;
import dev.lilcurio.output.ResultPrinter;
import dev.lilcurio.schema.SchemaSupport;
import dev.lilcurio.schema.SchemaType;
import dev.lilcurio.schema.SchemaTypeRegistry;
import io.apicurio.registry.content.TypedContent;
import io.apicurio.registry.rules.validity.ContentValidator;
import io.apicurio.registry.rules.validity.ValidityLevel;
import io.apicurio.registry.rules.violation.RuleViolationException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Callable;

@Command(
        name = "validate",
        mixinStandardHelpOptions = true,
        description = "Validate a schema file for syntactic/semantic correctness."
)
public class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the schema file to validate.")
    private Path schemaFile;

    @Option(names = {"-t", "--type"}, required = true,
            description = "Schema type: json (avro, protobuf planned).")
    private String type;

    @Option(names = {"-l", "--level"}, defaultValue = "full",
            description = "Validity level: none, syntax_only, full. Default: ${DEFAULT-VALUE}.")
    private String level;

    @Option(names = {"--json"}, defaultValue = "false",
            description = "Output results as JSON.")
    private boolean jsonOutput;

    @Override
    public Integer call() throws Exception {
        SchemaType schemaType = SchemaType.fromCliName(type);
        SchemaSupport support = SchemaTypeRegistry.get(schemaType);
        ValidityLevel validityLevel = parseValidityLevel(level);

        TypedContent content = SchemaFileReader.read(schemaFile, support.getContentType());
        ContentValidator validator = support.getContentValidator();

        ResultPrinter printer = ResultPrinter.create(jsonOutput);

        try {
            validator.validate(validityLevel, content, Collections.emptyMap());
            printer.printValidationSuccess(schemaFile, validityLevel);
            return 0;
        } catch (RuleViolationException e) {
            printer.printValidationFailure(schemaFile, validityLevel, e);
            return 1;
        }
    }

    private ValidityLevel parseValidityLevel(String level) {
        return switch (level.toLowerCase().replace("-", "_")) {
            case "none" -> ValidityLevel.NONE;
            case "syntax_only", "syntax" -> ValidityLevel.SYNTAX_ONLY;
            case "full" -> ValidityLevel.FULL;
            default -> throw new IllegalArgumentException(
                    "Invalid validity level: " + level
                            + ". Valid: none, syntax_only, full");
        };
    }
}
