package dev.lilcurio.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.registry.rules.compatibility.CompatibilityDifference;
import io.apicurio.registry.rules.compatibility.CompatibilityLevel;
import io.apicurio.registry.rules.validity.ValidityLevel;
import io.apicurio.registry.rules.violation.RuleViolation;
import io.apicurio.registry.rules.violation.RuleViolationException;

import java.nio.file.Path;
import java.util.Set;

public class JsonResultPrinter implements ResultPrinter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void printValidationSuccess(Path schemaFile, ValidityLevel level) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("status", "PASS");
        root.put("command", "validate");
        root.put("file", schemaFile.toString());
        root.put("level", level.toString());
        print(root);
    }

    @Override
    public void printValidationFailure(Path schemaFile, ValidityLevel level,
                                       RuleViolationException exception) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("status", "FAIL");
        root.put("command", "validate");
        root.put("file", schemaFile.toString());
        root.put("level", level.toString());

        ArrayNode violations = root.putArray("violations");
        Set<RuleViolation> causes = exception.getCauses();
        if (causes != null) {
            for (RuleViolation v : causes) {
                ObjectNode vNode = violations.addObject();
                vNode.put("description", v.getDescription());
                if (v.getContext() != null) {
                    vNode.put("context", v.getContext());
                }
            }
        }
        if ((causes == null || causes.isEmpty()) && exception.getMessage() != null) {
            ObjectNode vNode = violations.addObject();
            vNode.put("description", exception.getMessage());
        }
        print(root);
    }

    @Override
    public void printCompatibilitySuccess(Path proposedFile, CompatibilityLevel level) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("status", "PASS");
        root.put("command", "compatibility");
        root.put("file", proposedFile.toString());
        root.put("level", level.toString());
        print(root);
    }

    @Override
    public void printCompatibilityFailure(Path proposedFile, CompatibilityLevel level,
                                          Set<CompatibilityDifference> differences) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("status", "FAIL");
        root.put("command", "compatibility");
        root.put("file", proposedFile.toString());
        root.put("level", level.toString());

        ArrayNode violations = root.putArray("violations");
        if (differences != null) {
            for (CompatibilityDifference diff : differences) {
                RuleViolation rv = diff.asRuleViolation();
                ObjectNode vNode = violations.addObject();
                vNode.put("description", rv.getDescription());
                if (rv.getContext() != null) {
                    vNode.put("context", rv.getContext());
                }
            }
        }
        print(root);
    }

    private void print(ObjectNode node) {
        try {
            System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node));
        } catch (JsonProcessingException e) {
            System.err.println("{\"error\": \"Failed to serialize JSON output\"}");
        }
    }
}
