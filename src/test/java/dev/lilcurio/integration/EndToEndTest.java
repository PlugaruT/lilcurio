package dev.lilcurio.integration;

import dev.lilcurio.LilCurio;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class EndToEndTest {

    private int execute(String... args) {
        return new CommandLine(new LilCurio())
                .setExitCodeExceptionMapper(exception -> 2)
                .execute(args);
    }

    @Test
    void validateValidSchema() {
        int exitCode = execute("validate",
                "src/test/resources/schemas/json/valid-schema.json",
                "--type", "json", "--level", "full");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void validateInvalidSyntax() {
        int exitCode = execute("validate",
                "src/test/resources/schemas/json/invalid-syntax.json",
                "--type", "json", "--level", "syntax_only");
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void compatibilityBackwardPass() {
        int exitCode = execute("compatibility",
                "src/test/resources/schemas/json/schema-v1.json",
                "src/test/resources/schemas/json/schema-v2-compatible.json",
                "--type", "json", "--level", "backward");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void compatibilityBackwardFail() {
        int exitCode = execute("compatibility",
                "src/test/resources/schemas/json/schema-v1.json",
                "src/test/resources/schemas/json/schema-v2-incompatible.json",
                "--type", "json", "--level", "backward");
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void validateWithJsonOutput() {
        int exitCode = execute("validate",
                "src/test/resources/schemas/json/valid-schema.json",
                "--type", "json", "--json");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void noSubcommandShowsHelp() {
        int exitCode = execute();
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void helpFlag() {
        int exitCode = execute("--help");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void validateSubcommandHelp() {
        int exitCode = execute("validate", "--help");
        assertThat(exitCode).isEqualTo(0);
    }
}
