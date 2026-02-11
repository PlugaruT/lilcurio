package dev.lilcurio.output;

import io.apicurio.registry.rules.compatibility.CompatibilityDifference;
import io.apicurio.registry.rules.compatibility.CompatibilityLevel;
import io.apicurio.registry.rules.validity.ValidityLevel;
import io.apicurio.registry.rules.violation.RuleViolationException;

import java.nio.file.Path;
import java.util.Set;

public interface ResultPrinter {

    void printValidationSuccess(Path schemaFile, ValidityLevel level);

    void printValidationFailure(Path schemaFile, ValidityLevel level,
                                RuleViolationException exception);

    void printCompatibilitySuccess(Path proposedFile, CompatibilityLevel level);

    void printCompatibilityFailure(Path proposedFile, CompatibilityLevel level,
                                   Set<CompatibilityDifference> differences);

    static ResultPrinter create(boolean jsonOutput) {
        return jsonOutput ? new JsonResultPrinter() : new PlainTextResultPrinter();
    }
}
