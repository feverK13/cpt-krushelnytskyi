package ua.lpnu.kzp.data;

import ua.lpnu.kzp.logging.AppLogger;
import ua.lpnu.kzp.model.Product;
import ua.lpnu.kzp.model.ProductValidationException;
import ua.lpnu.kzp.model.RecordField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses the input lines into products and skipped-line messages.
 */
public final class InputProcessor {

    private InputProcessor() {
    }

    /**
     * Outcome of processing all input lines.
     *
     * @param products        every valid product, in file order
     * @param skippedMessages Ukrainian messages for every skipped line, in file order
     */
    public record ProcessedInput(List<Product> products, List<String> skippedMessages) {

        /** Creates the outcome with defensive copies of both lists. */
        public ProcessedInput {
            products = List.copyOf(products);
            skippedMessages = List.copyOf(skippedMessages);
        }
    }

    /**
     * Parses every line; a skipped line never stops processing.
     *
     * @param lines  raw input lines, in file order
     * @param logger logger receiving one warning per skipped line and the validation summary
     * @return the valid products and the skipped-line messages
     */
    public static ProcessedInput process(List<String> lines, AppLogger logger) {
        List<Product> products = new ArrayList<>();
        List<String> skippedMessages = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            try {
                products.add(Product.fromCsv(lines.get(index)));
            } catch (ProductValidationException e) {
                skippedMessages.add(message(lineNumber, e));
                RecordField field = e.field();
                logger.warn(
                        "InputProcessor.process",
                        lineNumber,
                        field == null ? null : field.csvName(),
                        "Skipped line: " + e.error().english(e.detail()));
            }
        }

        logger.info(
                "InputProcessor.process",
                "Validation summary: " + products.size() + " valid, " + skippedMessages.size() + " skipped");
        return new ProcessedInput(products, skippedMessages);
    }

    private static String message(int lineNumber, ProductValidationException e) {
        String reason = e.error().ukrainian(e.detail());
        if (e.field() == null) {
            return String.format(Locale.ROOT, "Рядок %d: %s", lineNumber, reason);
        }
        return String.format(Locale.ROOT, "Рядок %d: %s: %s", lineNumber, e.field().label(), reason);
    }
}
