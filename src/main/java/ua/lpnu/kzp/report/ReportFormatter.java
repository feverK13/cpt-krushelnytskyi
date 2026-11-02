package ua.lpnu.kzp.report;

import ua.lpnu.kzp.metrics.Metrics;
import ua.lpnu.kzp.metrics.WarrantyCostSummary;
import ua.lpnu.kzp.model.ProductDescriber;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Builds the Ukrainian report text shown on the console and written to the output file.
 */
public final class ReportFormatter {

    private static final String ROW = "%-34s %16s%n";
    private static final int TABLE_WIDTH = 51;

    private ReportFormatter() {
    }

    /**
     * Builds the whole report text.
     *
     * <p>The report holds a title, the input path, a fixed-width table with the four metrics (or
     * a "no valid records" message instead), the number of skipped lines and, if there are any,
     * the skipped-line messages in file order. Numbers use {@link Locale#ROOT} and two decimals;
     * lines end with the platform line separator.</p>
     *
     * @param input           path of the processed input file
     * @param metrics         computed metrics, or empty if there were no valid records
     * @param skippedMessages Ukrainian skipped-line messages, in file order
     * @return the report text, ending with a line separator
     */
    public static String format(Path input, Optional<Metrics> metrics, List<String> skippedMessages) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Звіт: електронний магазин%n"));
        report.append(String.format(Locale.ROOT, "Вхідний файл: %s%n%n", input));

        if (metrics.isPresent()) {
            Metrics m = metrics.get();
            report.append(String.format(Locale.ROOT, ROW, "Показник", "Значення"));
            report.append(String.format(Locale.ROOT, "%s%n", "-".repeat(TABLE_WIDTH)));
            report.append(String.format(
                    Locale.ROOT, ROW, "Кількість коректних записів", String.valueOf(m.validCount())));
            report.append(String.format(
                    Locale.ROOT, ROW, "Середня ціна",
                    String.format(Locale.ROOT, "%.2f грн", m.averagePrice())));
            report.append(String.format(
                    Locale.ROOT, ROW, "Найдовша гарантія, міс.",
                    String.valueOf(m.longestWarrantyMonths())));
            report.append(String.format(
                    Locale.ROOT, ROW, "Загальний запас, шт.", String.valueOf(m.totalStock())));
        } else {
            report.append(String.format(Locale.ROOT, "Немає жодного коректного запису.%n"));
        }

        report.append(String.format(Locale.ROOT, "%nПропущено рядків: %d%n", skippedMessages.size()));
        if (!skippedMessages.isEmpty()) {
            report.append(String.format(Locale.ROOT, "%nПропущені рядки:%n"));
            for (String message : skippedMessages) {
                report.append(String.format(Locale.ROOT, "%s%n", message));
            }
        }
        return report.toString();
    }

    /**
     * Builds the report of {@link #format(Path, Optional, List)} followed by the warranty cost
     * section.
     *
     * <p>The section starts with a blank line and the title {@code Гарантійна вартість:}. Each
     * group has a row with the kind label, the count and the subtotal, then its policy text from
     * {@link ProductDescriber}; a total row closes the section. Without groups the section says
     * that there is nothing to calculate.</p>
     *
     * @param input           path of the processed input file
     * @param metrics         computed metrics, or empty if there were no valid records
     * @param skippedMessages Ukrainian skipped-line messages, in file order
     * @param warrantyCost    warranty cost summary of the valid products
     * @return the report text, ending with a line separator
     */
    public static String format(
            Path input, Optional<Metrics> metrics, List<String> skippedMessages,
            WarrantyCostSummary warrantyCost) {
        return format(input, metrics, skippedMessages) + warrantyCostSection(warrantyCost);
    }

    private static String warrantyCostSection(WarrantyCostSummary summary) {
        StringBuilder section = new StringBuilder();
        section.append(String.format(Locale.ROOT, "%nГарантійна вартість:%n"));
        if (summary.groups().isEmpty()) {
            section.append(String.format(Locale.ROOT, "Немає коректних записів для розрахунку.%n"));
            return section.toString();
        }
        for (WarrantyCostSummary.Group group : summary.groups()) {
            section.append(String.format(
                    Locale.ROOT, ROW,
                    group.kind().label() + ", шт.: " + group.count(),
                    String.format(Locale.ROOT, "%.2f грн", group.subtotal())));
            section.append(String.format(
                    Locale.ROOT, "  %s%n", ProductDescriber.describe(group.representative())));
        }
        section.append(String.format(Locale.ROOT, "%s%n", "-".repeat(TABLE_WIDTH)));
        section.append(String.format(
                Locale.ROOT, ROW, "Разом", String.format(Locale.ROOT, "%.2f грн", summary.total())));
        return section.toString();
    }
}
