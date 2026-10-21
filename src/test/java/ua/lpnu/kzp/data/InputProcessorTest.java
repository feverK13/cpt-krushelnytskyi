package ua.lpnu.kzp.data;

import ua.lpnu.kzp.logging.AppLogger;
import ua.lpnu.kzp.metrics.Metrics;
import ua.lpnu.kzp.metrics.MetricsCalculator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link InputProcessor}: the product list, skipped-line messages with line numbers and
 * the log records written for skipped lines.
 */
class InputProcessorTest {

    private static final String VALID = "Монітор;Монітори;6499.00;36;12";

    @TempDir
    Path tempDir;

    private InputProcessor.ProcessedInput process(List<String> lines) {
        Path logFile = tempDir.resolve("app.log");
        PrintStream err = new PrintStream(PrintStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        try (AppLogger logger = AppLogger.create(logFile, false, err)) {
            return InputProcessor.process(lines, logger);
        }
    }

    private static List<String> linesWithAt(int lineNumber, String line) {
        List<String> lines = new ArrayList<>(Collections.nCopies(lineNumber - 1, VALID));
        lines.add(line);
        return lines;
    }

    @Test
    void collectsValidProductsInFileOrder() {
        InputProcessor.ProcessedInput result = process(List.of(
                "А;К;1;1;1", "Б;К;2;2;2", "В;К;3;3;3"));

        assertEquals(List.of("А", "Б", "В"),
                result.products().stream().map(p -> p.getName()).toList());
        assertTrue(result.skippedMessages().isEmpty());
    }

    @Test
    void addsTheLineNumberAtTheReadingLevel() {
        InputProcessor.ProcessedInput result = process(linesWithAt(42, "   "));

        assertEquals(List.of("Рядок 42: порожній рядок"), result.skippedMessages());
        assertEquals(41, result.products().size());
    }

    static Stream<Arguments> ukrainianMessages() {
        return Stream.of(
                Arguments.of(7, "   ", "Рядок 7: порожній рядок"),
                Arguments.of(2, "a;b;c;d", "Рядок 2: очікується 5 полів, отримано 4"),
                Arguments.of(3, ";Монітори;1;1;1", "Рядок 3: назва: порожнє поле"),
                Arguments.of(4, "Монітор;;1;1;1", "Рядок 4: категорія: порожнє поле"),
                Arguments.of(
                        5, "Монітор;Монітори;абв;1;1", "Рядок 5: ціна: не є числом: \"абв\""),
                Arguments.of(
                        6, "Монітор;Монітори;0;1;1", "Рядок 6: ціна: має бути більше нуля: \"0\""),
                Arguments.of(
                        8, "Монітор;Монітори;-1;1;1", "Рядок 8: ціна: від'ємне значення: \"-1\""),
                Arguments.of(
                        9,
                        "Монітор;Монітори;1;12.5;1",
                        "Рядок 9: гарантія: має бути цілим числом: \"12.5\""),
                Arguments.of(
                        10, "Монітор;Монітори;1;1;-3", "Рядок 10: запас: від'ємне значення: \"-3\""),
                Arguments.of(
                        11,
                        "Монітор;Монітори;1;1;2147483648",
                        "Рядок 11: запас: завелике значення: \"2147483648\""));
    }

    @ParameterizedTest
    @MethodSource("ukrainianMessages")
    void rendersUkrainianMessage(int lineNumber, String line, String expectedMessage) {
        InputProcessor.ProcessedInput result = process(linesWithAt(lineNumber, line));

        assertEquals(List.of(expectedMessage), result.skippedMessages());
    }

    @Test
    void invalidLinesNeverEnterTheListAndKeepFileOrder() {
        InputProcessor.ProcessedInput result = process(List.of(
                VALID, "Монітор;Монітори;абв;1;1", "", "a;b;c;d", VALID, "Монітор;Монітори;1;1;-3"));

        assertEquals(2, result.products().size());
        assertEquals(
                List.of(
                        "Рядок 2: ціна: не є числом: \"абв\"",
                        "Рядок 3: порожній рядок",
                        "Рядок 4: очікується 5 полів, отримано 4",
                        "Рядок 6: запас: від'ємне значення: \"-3\""),
                result.skippedMessages());
    }

    @Test
    void invalidLinesDoNotChangeTheMetrics() {
        List<String> valid = List.of(
                "А;К;100.00;12;5", "Б;К;200.50;24;0", "В;К;300.25;6;10");
        List<String> mixed = List.of(
                "А;К;100.00;12;5", "Б;К;абв;1;1", "Б;К;200.50;24;0", "", "Г;К;0;1;1",
                "В;К;300.25;6;10", "Д;К;1;1;-1", "a;b;c");

        Metrics expected = MetricsCalculator.calculate(process(valid).products()).orElseThrow();
        Metrics actual = MetricsCalculator.calculate(process(mixed).products()).orElseThrow();

        assertEquals(expected, actual);
        assertEquals(3, actual.validCount());
    }

    @Test
    void logsEverySkippedLineWithLineAndField() throws IOException {
        process(List.of(VALID, ";Монітори;1;1;1", "a;b;c;d"));

        String log = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertTrue(log.contains(
                "WARN | InputProcessor.process | line=2 field=name | Skipped line: empty field"), log);
        assertTrue(log.contains(
                "WARN | InputProcessor.process | line=3 field=- | Skipped line: expected 5 fields, got 4"),
                log);
        assertTrue(log.contains("Validation summary: 1 valid, 2 skipped"), log);
    }
}
