package ua.lpnu.kzp.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of the {@code --warranty-cost} option of {@link CliParser}. */
class CliParserWarrantyCostTest {

    private static CliParseResult.Run run(String... args) {
        return assertInstanceOf(CliParseResult.Run.class, CliParser.parse(args));
    }

    @Test
    void absentByDefault() {
        assertFalse(run().warrantyCost());
        assertFalse(run("--verbose").warrantyCost());
    }

    @Test
    void presentWhenGiven() {
        assertTrue(run("--warranty-cost").warrantyCost());
    }

    @Test
    void combinesWithOtherOptionsInAnyPosition() {
        for (CliParseResult.Run run : new CliParseResult.Run[] {
            run("--warranty-cost", "--input", "a.csv", "--output", "b.txt", "--verbose"),
            run("--input", "a.csv", "--warranty-cost", "--output", "b.txt", "--verbose"),
            run("--input", "a.csv", "--output", "b.txt", "--verbose", "--warranty-cost")}) {
            assertTrue(run.warrantyCost());
            assertTrue(run.verbose());
            assertEquals(Path.of("a.csv"), run.input());
            assertEquals(Path.of("b.txt"), run.output());
        }
    }

    @Test
    void repeatedFlagIsAccepted() {
        assertTrue(run("--warranty-cost", "--warranty-cost").warrantyCost());
    }

    @Test
    void helpStillWinsAndUsageMentionsTheFlag() {
        assertInstanceOf(CliParseResult.Help.class, CliParser.parse(new String[] {"--warranty-cost", "--help"}));
        assertTrue(CliParser.usage().contains("--warranty-cost"));
    }

    @Test
    void optionBeforeAMissingValueStillReportsTheMissingValue() {
        assertInstanceOf(CliParseResult.Error.class, CliParser.parse(new String[] {"--warranty-cost", "--input"}));
    }

    @Test
    void shortRunConstructorHasNoWarrantyCost() {
        assertFalse(new CliParseResult.Run(Path.of("a"), Path.of("b"), false).warrantyCost());
    }
}
