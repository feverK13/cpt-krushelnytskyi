package ua.lpnu.kzp.cli;

import java.nio.file.Path;

/**
 * Outcome of parsing the command line arguments.
 */
public sealed interface CliParseResult {

    /** The user requested usage help ({@code --help}). */
    record Help() implements CliParseResult {
    }

    /** The user requested the application version ({@code --version}). */
    record Version() implements CliParseResult {
    }

    /**
     * Arguments were parsed successfully into a runnable configuration.
     *
     * @param input   resolved input CSV path
     * @param output  resolved report output path
     * @param verbose whether log records must also be mirrored to stderr
     * @param warrantyCost whether the warranty cost section must be appended to the report
     */
    record Run(Path input, Path output, boolean verbose, boolean warrantyCost)
            implements CliParseResult {

        /** Creates a configuration without the warranty cost section. */
        public Run(Path input, Path output, boolean verbose) {
            this(input, output, verbose, false);
        }
    }

    /**
     * Arguments could not be parsed.
     *
     * @param message Ukrainian error message, ready to print to stderr
     */
    record Error(String message) implements CliParseResult {
    }
}
