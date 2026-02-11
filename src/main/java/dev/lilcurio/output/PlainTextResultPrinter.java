package dev.lilcurio.output;

import io.apicurio.registry.rules.compatibility.CompatibilityDifference;
import io.apicurio.registry.rules.compatibility.CompatibilityLevel;
import io.apicurio.registry.rules.validity.ValidityLevel;
import io.apicurio.registry.rules.violation.RuleViolation;
import io.apicurio.registry.rules.violation.RuleViolationException;

import java.nio.file.Path;
import java.util.Set;

public class PlainTextResultPrinter implements ResultPrinter {

    @Override
    public void printValidationSuccess(Path schemaFile, ValidityLevel level) {
        System.out.println("PASS: Schema validation succeeded for "
                + schemaFile + " (level: " + level + ")");
    }

    @Override
    public void printValidationFailure(Path schemaFile, ValidityLevel level,
                                       RuleViolationException exception) {
        System.err.println("FAIL: Schema validation failed for "
                + schemaFile + " (level: " + level + ")");

        Set<RuleViolation> causes = exception.getCauses();
        if (causes != null && !causes.isEmpty()) {
            System.err.println("Violations:");
            for (RuleViolation v : causes) {
                String context = v.getContext() != null ? " [at: " + v.getContext() + "]" : "";
                System.err.println("  - " + v.getDescription() + context);
            }
        } else if (exception.getMessage() != null) {
            System.err.println("  - " + exception.getMessage());
        }
    }

    @Override
    public void printCompatibilitySuccess(Path proposedFile, CompatibilityLevel level) {
        System.out.println("PASS: Compatibility check succeeded for "
                + proposedFile + " (level: " + level + ")");
    }

    @Override
    public void printCompatibilityFailure(Path proposedFile, CompatibilityLevel level,
                                          Set<CompatibilityDifference> differences) {
        System.err.println("FAIL: Compatibility check failed for "
                + proposedFile + " (level: " + level + ")");

        if (differences != null && !differences.isEmpty()) {
            System.err.println("Incompatible differences:");
            for (CompatibilityDifference diff : differences) {
                RuleViolation rv = diff.asRuleViolation();
                String context = rv.getContext() != null ? " [at: " + rv.getContext() + "]" : "";
                System.err.println("  - " + rv.getDescription() + context);
            }
        }
    }
}
