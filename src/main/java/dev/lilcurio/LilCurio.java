package dev.lilcurio;

import dev.lilcurio.command.CompatibilityCommand;
import dev.lilcurio.command.DiffCommand;
import dev.lilcurio.command.ValidateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "lil-curio",
        mixinStandardHelpOptions = true,
        version = "lil-curio 0.1.0",
        description = "Offline schema validation and compatibility checking " +
                "using Apicurio Registry internals.",
        subcommands = {
                ValidateCommand.class,
                CompatibilityCommand.class,
                DiffCommand.class
        }
)
public class LilCurio implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LilCurio())
                .setExitCodeExceptionMapper(exception -> 2)
                .execute(args);
        System.exit(exitCode);
    }
}
