package ua.lpnu.kzp.report;

import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.metrics.Metrics;
import ua.lpnu.kzp.metrics.MetricsCalculator;
import ua.lpnu.kzp.metrics.WarrantyCostSummary;
import ua.lpnu.kzp.model.Product;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of the warranty cost section of {@link ReportFormatter}. */
class ReportFormatterWarrantyCostTest {

    private static final Path INPUT = Path.of("in.csv");

    @Test
    void sectionIsAppendedToTheUnchangedReport() {
        List<Product> products = List.of(
                Product.of("l", "Ноутбуки", 40000, 24, 1),
                Product.of("p", "Монітори", 1500, 12, 1),
                Product.of("o", "Телевізори", 1000, 6, 1));
        Optional<Metrics> metrics = MetricsCalculator.calculate(products);
        String plain = ReportFormatter.format(INPUT, metrics, List.of());

        String withSection = ReportFormatter.format(
                INPUT, metrics, List.of(), WarrantyCostSummary.of(products));

        String nl = String.format(Locale.ROOT, "%n");
        String expectedSection = nl + "Гарантійна вартість:" + nl
                + String.format(Locale.ROOT, "%-34s %16s", "Ноутбук, шт.: 1", "2600.00 грн") + nl
                + "  3% ціни за кожен рік гарантії + 100 грн за кожен розпочатий рік" + nl
                + String.format(Locale.ROOT, "%-34s %16s", "Периферія, шт.: 1", "15.00 грн") + nl
                + "  1% ціни за кожен рік гарантії" + nl
                + String.format(Locale.ROOT, "%-34s %16s", "Інше, шт.: 1", "10.00 грн") + nl
                + "  2% ціни за кожен рік гарантії" + nl
                + "-".repeat(51) + nl
                + String.format(Locale.ROOT, "%-34s %16s", "Разом", "2625.00 грн") + nl;
        assertEquals(plain + expectedSection, withSection);
    }

    @Test
    void emptySectionSaysThereIsNothingToCalculate() {
        String plain = ReportFormatter.format(INPUT, Optional.empty(), List.of());

        String withSection = ReportFormatter.format(
                INPUT, Optional.empty(), List.of(), WarrantyCostSummary.of(List.of()));

        String nl = String.format(Locale.ROOT, "%n");
        assertEquals(plain + nl + "Гарантійна вартість:" + nl
                + "Немає коректних записів для розрахунку." + nl, withSection);
        assertTrue(withSection.contains("Немає жодного коректного запису."));
    }
}
