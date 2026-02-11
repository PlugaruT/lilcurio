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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "diff",
        mixinStandardHelpOptions = true,
        description = "Check compatibility of a schema file against its last committed version in git. " +
                "Uses 'git show' to retrieve the previous version from HEAD."
)
public class DiffCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the schema file (must be in a git repo).")
    private Path schemaFile;

    @Option(names = {"-t", "--type"}, required = true,
            description = "Schema type: json (avro, protobuf planned).")
    private String type;

    @Option(names = {"-l", "--level"}, defaultValue = "backward",
            description = "Compatibility level: backward, backward-transitive, " +
                    "forward, forward-transitive, full, full-transitive, none. " +
                    "Default: ${DEFAULT-VALUE}.")
    private String level;

    @Option(names = {"--ref"}, defaultValue = "HEAD",
            description = "Git ref to compare against. Default: ${DEFAULT-VALUE}.")
    private String gitRef;

    @Option(names = {"--json"}, defaultValue = "false",
            description = "Output results as JSON.")
    private boolean jsonOutput;

    @Override
    public Integer call() throws Exception {
        SchemaType schemaType = SchemaType.fromCliName(type);
        SchemaSupport support = SchemaTypeRegistry.get(schemaType);
        CompatibilityLevel compatLevel = parseCompatibilityLevel(level);
        String contentType = support.getContentType();

        String previousContent = gitShow(gitRef, schemaFile);
        if (previousContent == null) {
            System.err.println("Error: File not found in git at " + gitRef + ":" + schemaFile);
            System.err.println("Is this file tracked by git?");
            return 2;
        }

        TypedContent existing = TypedContent.create(previousContent, contentType);
        TypedContent proposed = SchemaFileReader.read(schemaFile, contentType);

        CompatibilityChecker checker = support.getCompatibilityChecker();
        CompatibilityExecutionResult result = checker.testCompatibility(
                compatLevel, List.of(existing), proposed, java.util.Collections.emptyMap());

        ResultPrinter printer = ResultPrinter.create(jsonOutput);

        if (result.isCompatible()) {
            printer.printCompatibilitySuccess(schemaFile, compatLevel);
            return 0;
        } else {
            printer.printCompatibilityFailure(
                    schemaFile, compatLevel, result.getIncompatibleDifferences());
            return 1;
        }
    }

    private String gitShow(String ref, Path filePath) throws IOException, InterruptedException {
        // Resolve to repo-relative path (toRealPath resolves symlinks like /var -> /private/var)
        Path repoRoot = findGitRoot(filePath);
        Path realFile = filePath.toAbsolutePath().toRealPath();
        String relativePath;
        if (repoRoot != null) {
            relativePath = repoRoot.relativize(realFile).toString();
        } else {
            relativePath = filePath.toString();
        }

        ProcessBuilder pb = new ProcessBuilder("git", "show", ref + ":" + relativePath);
        if (repoRoot != null) {
            pb.directory(repoRoot.toFile());
        }
        pb.redirectErrorStream(false);

        Process process = pb.start();
        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            return null;
        }
        return stdout;
    }

    private Path findGitRoot(Path from) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "--show-toplevel");
        pb.directory(from.toAbsolutePath().getParent().toFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();
        String stdout = new String(process.getInputStream().readAllBytes()).trim();
        int exitCode = process.waitFor();

        if (exitCode != 0 || stdout.isEmpty()) {
            return null;
        }
        return Path.of(stdout);
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
