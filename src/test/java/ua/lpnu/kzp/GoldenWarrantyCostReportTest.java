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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the application on {@code data/input.csv} with {@code --warranty-cost} and compares the
 * output with the stored report; the Lab 1 report must stay its prefix.
 */
class GoldenWarrantyCostReportTest {

    private static final String INPUT_LINE_PREFIX = "Вхідний файл:";

    @TempDir
    Path tempDir;

    private static String normalize(String text) {
        return Arrays.stream(text.replace("\r\n", "\n").split("\n", -1))
                .map(line -> line.startsWith(INPUT_LINE_PREFIX) ? line.replace('\\', '/') : line)
                .collect(Collectors.joining("\n"));
    }

    private static String resource(String name) throws IOException {
        try (InputStream in = GoldenWarrantyCostReportTest.class.getResourceAsStream(name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String run(String... args) throws IOException {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        Path reportFile = tempDir.resolve("report.txt");
        List<String> all = new ArrayList<>(List.of(
                "--input", Path.of("data", "input.csv").toString(), "--output", reportFile.toString()));
        all.addAll(Arrays.asList(args));

        int exitCode = Main.run(all.toArray(new String[0]), out, err, tempDir.resolve("app.log"));

        assertEquals(0, exitCode);
        String console = normalize(outBuffer.toString(StandardCharsets.UTF_8));
        assertEquals(console, normalize(Files.readString(reportFile, StandardCharsets.UTF_8)));
        return console;
    }

    @Test
    void flagOutputEqualsTheStoredReport() throws IOException {
        assertEquals(normalize(resource("/baseline/lab3-warranty-cost-report.txt")), run("--warranty-cost"));
    }

    @Test
    void flagOutputStartsWithTheUnchangedLab1Report() throws IOException {
        assertTrue(run("--warranty-cost").startsWith(normalize(resource("/baseline/lab1-report.txt"))));
    }

    @Test
    void withoutTheFlagTheOutputIsTheLab1Report() throws IOException {
        assertEquals(normalize(resource("/baseline/lab1-report.txt")), run());
    }

    @Test
    void flagPositionDoesNotMatter() throws IOException {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);

        int exitCode = Main.run(
                new String[] {"--warranty-cost", "--input", Path.of("data", "input.csv").toString(),
                    "--output", tempDir.resolve("r2.txt").toString()},
                out, err, tempDir.resolve("app2.log"));

        assertEquals(0, exitCode);
        assertEquals(normalize(resource("/baseline/lab3-warranty-cost-report.txt")),
                normalize(outBuffer.toString(StandardCharsets.UTF_8)));
    }
}
