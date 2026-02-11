package dev.lilcurio.command;

import dev.lilcurio.io.SchemaFileReader;
import dev.lilcurio.output.ResultPrinter;
import dev.lilcurio.schema.SchemaSupport;
import dev.lilcurio.schema.SchemaType;
import dev.lilcurio.schema.SchemaTypeRegistry;
import io.apicurio.registry.content.TypedContent;
import io.apicurio.registry.rules.compatibility.CompatibilityChecker;
import io.apicurio.registry.rules.compatibility.CompatibilityExecutionResult;
import io.apicurio.registry.rules.compatibility.CompatibilityLevel;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "compatibility",
        mixinStandardHelpOptions = true,
        description = "Check compatibility between schema versions. " +
                "The last file is the proposed new version; all preceding files are existing versions."
)
public class CompatibilityCommand implements Callable<Integer> {

    @Parameters(arity = "2..*",
            description = "Schema files: <existing...> <proposed>. " +
                    "The last file is the proposed new version.")
    private List<Path> schemaFiles;

    @Option(names = {"-t", "--type"}, required = true,
            description = "Schema type: json (avro, protobuf planned).")
    private String type;

    @Option(names = {"-l", "--level"}, defaultValue = "backward",
            description = "Compatibility level: backward, backward-transitive, " +
                    "forward, forward-transitive, full, full-transitive, none. " +
                    "Default: ${DEFAULT-VALUE}.")
    private String level;

    @Option(names = {"--json"}, defaultValue = "false",
            description = "Output results as JSON.")
    private boolean jsonOutput;

    @Override
    public Integer call() throws Exception {
        SchemaType schemaType = SchemaType.fromCliName(type);
        SchemaSupport support = SchemaTypeRegistry.get(schemaType);
        CompatibilityLevel compatLevel = parseCompatibilityLevel(level);
        String contentType = support.getContentType();

        Path proposedPath = schemaFiles.get(schemaFiles.size() - 1);
        TypedContent proposed = SchemaFileReader.read(proposedPath, contentType);

        List<TypedContent> existing = new ArrayList<>();
        for (int i = 0; i < schemaFiles.size() - 1; i++) {
            existing.add(SchemaFileReader.read(schemaFiles.get(i), contentType));
        }

        CompatibilityChecker checker = support.getCompatibilityChecker();
        CompatibilityExecutionResult result = checker.testCompatibility(
                compatLevel, existing, proposed, Collections.emptyMap());

        ResultPrinter printer = ResultPrinter.create(jsonOutput);

        if (result.isCompatible()) {
            printer.printCompatibilitySuccess(proposedPath, compatLevel);
            return 0;
        } else {
            printer.printCompatibilityFailure(
                    proposedPath, compatLevel, result.getIncompatibleDifferences());
            return 1;
        }
    }

    private CompatibilityLevel parseCompatibilityLevel(String level) {
        return switch (level.toLowerCase().replace("-", "_")) {
            case "backward" -> CompatibilityLevel.BACKWARD;
            case "backward_transitive" -> CompatibilityLevel.BACKWARD_TRANSITIVE;
            case "forward" -> CompatibilityLevel.FORWARD;
            case "forward_transitive" -> CompatibilityLevel.FORWARD_TRANSITIVE;
            case "full" -> CompatibilityLevel.FULL;
            case "full_transitive" -> CompatibilityLevel.FULL_TRANSITIVE;
            case "none" -> CompatibilityLevel.NONE;
            default -> throw new IllegalArgumentException(
                    "Invalid compatibility level: " + level
                            + ". Valid: backward, backward-transitive, forward, forward-transitive, full, full-transitive, none");
        };
    }
}
