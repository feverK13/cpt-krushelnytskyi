package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the application on {@code data/input.csv} and compares the console output and the report
 * file with the stored Lab 1 report.
 */
class GoldenReportTest {

    private static final String INPUT_LINE_PREFIX = "Вхідний файл:";

    @TempDir
    Path tempDir;

    private static String normalize(String text) {
        // Limit -1 keeps a trailing empty element, so a missing final newline stays visible.
        return Arrays.stream(text.replace("\r\n", "\n").split("\n", -1))
                .map(line -> line.startsWith(INPUT_LINE_PREFIX) ? line.replace('\\', '/') : line)
                .collect(Collectors.joining("\n"));
    }

    private static String golden() throws IOException {
        try (InputStream in = GoldenReportTest.class.getResourceAsStream("/baseline/lab1-report.txt")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void consoleAndReportFileEqualTheLab1Baseline() throws IOException {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        Path reportFile = tempDir.resolve("report.txt");

        int exitCode = Main.run(
                new String[] {"--input", Path.of("data", "input.csv").toString(),
                    "--output", reportFile.toString()},
                out, err, tempDir.resolve("app.log"));

        String expected = normalize(golden());
        String baselineExitCode =
                Files.readString(Path.of("docs", "baseline", "lab1", "exit-code.txt"), StandardCharsets.UTF_8);
        assertEquals(0, exitCode);
        assertEquals(Integer.parseInt(baselineExitCode.strip()), exitCode);
        assertEquals(expected, normalize(outBuffer.toString(StandardCharsets.UTF_8)));
        assertEquals(expected, normalize(Files.readString(reportFile, StandardCharsets.UTF_8)));
    }
}
