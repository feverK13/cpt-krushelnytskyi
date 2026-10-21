package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link Product#fromCsv}: parsing, every validation rule with the raw-text
 * messages of Lab 1, and the order in which fields are checked.
 */
class ProductFromCsvTest {

    private static final String VALID_PREFIX = "Монітор;Монітори;";

    private static ProductValidationException rejected(String line) {
        return assertThrows(ProductValidationException.class, () -> Product.fromCsv(line));
    }

    private static void assertRejection(
            String line, RecordField field, ValidationError error, String detail) {
        ProductValidationException e = rejected(line);

        assertEquals(field, e.field());
        assertEquals(error, e.error());
        assertEquals(detail, e.detail());
    }

    @Test
    void parsesValidLine() {
        Product product = Product.fromCsv("Монітор Dell S2425H;Монітори;6499.00;36;12");

        assertEquals("Монітор Dell S2425H", product.getName());
        assertEquals("Монітори", product.getCategory());
        assertEquals(6499.00, product.getPrice());
        assertEquals(36, product.getWarrantyMonths());
        assertEquals(12, product.getStock());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Монітор Dell S2425H;Монітори;6499.00;36;12",
        "Монітор;Монітори;0.01;0;0",
        "Монітор;Монітори;1;1;1",
        "Монітор;Монітори;2147483647;2147483647;2147483647",
        "Монітор;Монітори;1.5;-0;0"
    })
    void acceptsWellFormedRecords(String line) {
        assertNotNull(Product.fromCsv(line));
    }

    @Test
    void trimsEveryFieldAndKeepsCyrillic() {
        Product product =
                Product.fromCsv("  Пральна машина Bosch  ;  Побутова техніка  ;  18750.00  ;  36  ;  3  ");

        assertEquals("Пральна машина Bosch", product.getName());
        assertEquals("Побутова техніка", product.getCategory());
        assertEquals(18750.00, product.getPrice());
        assertEquals(36, product.getWarrantyMonths());
        assertEquals(3, product.getStock());
    }

    @ParameterizedTest
    @ValueSource(strings = {" 5 ", "\t5\t", " \t5\t "})
    void acceptsNumbersSurroundedBySpacesOrTabs(String number) {
        Product product =
                Product.fromCsv("Монітор\t;\tМонітори;" + number + ";" + number + ";" + number);

        assertEquals("Монітор", product.getName());
        assertEquals("Монітори", product.getCategory());
        assertEquals(5.0, product.getPrice());
        assertEquals(5, product.getWarrantyMonths());
        assertEquals(5, product.getStock());
    }

    @Test
    void acceptsNegativeZeroWarranty() {
        assertEquals(0, Product.fromCsv("Монітор;Монітори;1.5;-0;0").getWarrantyMonths());
    }

    @Test
    void rejectsNullLine() {
        assertThrows(NullPointerException.class, () -> Product.fromCsv(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", " \t "})
    void rejectsBlankLineBeforeAnyOtherCheck(String line) {
        assertRejection(line, null, ValidationError.EMPTY_LINE, null);
    }

    static Stream<Arguments> wrongFieldCounts() {
        return Stream.of(
                Arguments.of("a", 1),
                Arguments.of("a;b;c;d", 4),
                Arguments.of("a;b;c;d;e;", 6),
                Arguments.of("Монітор;Монітори;1;1;1;", 6),
                Arguments.of("a;b;c;d;e;f;g", 7),
                Arguments.of("abc", 1),
                Arguments.of("a;;;", 4),
                Arguments.of(" a ; b ", 2));
    }

    @ParameterizedTest
    @MethodSource("wrongFieldCounts")
    void rejectsWrongFieldCountWithActualCount(String line, int expectedCount) {
        assertRejection(line, null, ValidationError.FIELD_COUNT, String.valueOf(expectedCount));
    }

    @Test
    void trailingEmptyFieldCountsAsSixthField() {
        assertRejection(VALID_PREFIX + "1;1;1;", null, ValidationError.FIELD_COUNT, "6");
    }

    @Test
    void fieldCountMessageStatesFiveExpectedFields() {
        ProductValidationException e = rejected("a;b;c;d");

        assertEquals("expected 5 fields, got 4", e.error().english(e.detail()));
        assertEquals("очікується 5 полів, отримано 4", e.error().ukrainian(e.detail()));
    }

    static Stream<Arguments> emptyFields() {
        return Stream.of(
                Arguments.of(";Монітори;1;1;1", RecordField.NAME),
                Arguments.of("   ;Монітори;1;1;1", RecordField.NAME),
                Arguments.of("Монітор;;1;1;1", RecordField.CATEGORY),
                Arguments.of("Монітор;   ;1;1;1", RecordField.CATEGORY),
                Arguments.of("Монітор;Монітори;;1;1", RecordField.PRICE),
                Arguments.of("Монітор;Монітори; ;1;1", RecordField.PRICE),
                Arguments.of("Монітор;Монітори;1;;1", RecordField.WARRANTY_MONTHS),
                Arguments.of("Монітор;Монітори;1;1;", RecordField.STOCK),
                Arguments.of("Монітор;Монітори;1;1;  ", RecordField.STOCK));
    }

    @ParameterizedTest
    @MethodSource("emptyFields")
    void rejectsEmptyField(String line, RecordField expectedField) {
        assertRejection(line, expectedField, ValidationError.EMPTY_FIELD, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "абв", "12,5", "1e3", "1E3", "1e+3", "NaN", "Infinity", "-Infinity",
        "12.5f", "12.5d", "+5", ".5", "5.", "1 000", "1.2.3", "0x10", "--1"
    })
    void rejectsPriceThatIsNotAPlainDecimal(String price) {
        assertRejection(
                VALID_PREFIX + price + ";1;1", RecordField.PRICE, ValidationError.NOT_A_NUMBER, price);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "0.00", "-0", "-0.0"})
    void rejectsNonPositivePrice(String price) {
        assertRejection(
                VALID_PREFIX + price + ";1;1", RecordField.PRICE, ValidationError.NOT_POSITIVE, price);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0.01", "-24999.00"})
    void rejectsNegativePrice(String price) {
        assertRejection(
                VALID_PREFIX + price + ";1;1", RecordField.PRICE, ValidationError.NEGATIVE, price);
    }

    @Test
    void rejectsPriceThatDoesNotFitIntoDouble() {
        String price = "1".repeat(400);

        assertRejection(
                VALID_PREFIX + price + ";1;1", RecordField.PRICE, ValidationError.TOO_LARGE, price);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.5", "24.0", "0.5"})
    void rejectsNonIntegerWarranty(String warranty) {
        assertRejection(
                VALID_PREFIX + "1;" + warranty + ";1",
                RecordField.WARRANTY_MONTHS,
                ValidationError.NOT_AN_INTEGER,
                warranty);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.5", "1.0", "0.5"})
    void rejectsNonIntegerStock(String stock) {
        assertRejection(
                VALID_PREFIX + "1;1;" + stock,
                RecordField.STOCK,
                ValidationError.NOT_AN_INTEGER,
                stock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"абв", "+5", "1e3", "NaN", "12,5", "24m"})
    void rejectsWarrantyThatIsNotANumber(String warranty) {
        assertRejection(
                VALID_PREFIX + "1;" + warranty + ";1",
                RecordField.WARRANTY_MONTHS,
                ValidationError.NOT_A_NUMBER,
                warranty);
    }

    @ParameterizedTest
    @ValueSource(strings = {"абв", "+5", "1e3", "12,5"})
    void rejectsStockThatIsNotANumber(String stock) {
        assertRejection(
                VALID_PREFIX + "1;1;" + stock,
                RecordField.STOCK,
                ValidationError.NOT_A_NUMBER,
                stock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-24", "-03", "-2147483649"})
    void rejectsNegativeWarranty(String warranty) {
        assertRejection(
                VALID_PREFIX + "1;" + warranty + ";1",
                RecordField.WARRANTY_MONTHS,
                ValidationError.NEGATIVE,
                warranty);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-3", "-03", "-2147483649"})
    void rejectsNegativeStock(String stock) {
        assertRejection(
                VALID_PREFIX + "1;1;" + stock,
                RecordField.STOCK,
                ValidationError.NEGATIVE,
                stock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "99999999999999999999"})
    void rejectsWarrantyThatOverflowsInt(String warranty) {
        assertRejection(
                VALID_PREFIX + "1;" + warranty + ";1",
                RecordField.WARRANTY_MONTHS,
                ValidationError.TOO_LARGE,
                warranty);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "99999999999999999999"})
    void rejectsStockThatOverflowsInt(String stock) {
        assertRejection(
                VALID_PREFIX + "1;1;" + stock,
                RecordField.STOCK,
                ValidationError.TOO_LARGE,
                stock);
    }

    static Stream<Arguments> rawTextMessages() {
        return Stream.of(
                Arguments.of("Монітор;Монітори;0.00;1;1", "ціна: має бути більше нуля: \"0.00\""),
                Arguments.of("Монітор;Монітори;0;1;1", "ціна: має бути більше нуля: \"0\""),
                Arguments.of("Монітор;Монітори;-0;1;1", "ціна: має бути більше нуля: \"-0\""),
                Arguments.of("Монітор;Монітори;-0.0;1;1", "ціна: має бути більше нуля: \"-0.0\""),
                Arguments.of("Монітор;Монітори;-0.01;1;1", "ціна: від'ємне значення: \"-0.01\""),
                Arguments.of("Монітор;Монітори;1;-03;1", "гарантія: від'ємне значення: \"-03\""),
                Arguments.of("Монітор;Монітори;1;1;-03", "запас: від'ємне значення: \"-03\""),
                Arguments.of("Монітор;Монітори;1;1;-3", "запас: від'ємне значення: \"-3\""),
                Arguments.of("Монітор;Монітори;абв;1;1", "ціна: не є числом: \"абв\""),
                Arguments.of("Монітор;Монітори;1;12.5;1", "гарантія: має бути цілим числом: \"12.5\""),
                Arguments.of(
                        "Монітор;Монітори;1;1;2147483648",
                        "запас: завелике значення: \"2147483648\""));
    }

    @ParameterizedTest
    @MethodSource("rawTextMessages")
    void quotesTheRawFieldTextInTheMessage(String line, String expectedMessage) {
        ProductValidationException e = rejected(line);

        assertEquals(expectedMessage, e.field().label() + ": " + e.error().ukrainian(e.detail()));
    }

    static Stream<Arguments> firstFailingField() {
        return Stream.of(
                Arguments.of(";;абв;абв;абв", RecordField.NAME),
                Arguments.of("Монітор;;абв;абв;абв", RecordField.CATEGORY),
                Arguments.of("Монітор;Монітори;абв;абв;абв", RecordField.PRICE),
                Arguments.of("Монітор;Монітори;1;абв;абв", RecordField.WARRANTY_MONTHS),
                Arguments.of("Монітор;Монітори;1;1;абв", RecordField.STOCK),
                Arguments.of(" ;Монітори;0;-1;-1", RecordField.NAME),
                Arguments.of("Монітор;Монітори;0;-1;-1", RecordField.PRICE),
                Arguments.of("Монітор;Монітори;1;-1;-1", RecordField.WARRANTY_MONTHS));
    }

    @ParameterizedTest
    @MethodSource("firstFailingField")
    void reportsOnlyTheFirstFailingField(String line, RecordField expectedField) {
        assertEquals(expectedField, rejected(line).field());
    }

    @Test
    void blankNameWinsOverBadPrice() {
        assertRejection(" ;Монітори;абв;1;1", RecordField.NAME, ValidationError.EMPTY_FIELD, null);
    }

    static Stream<Arguments> englishReasons() {
        return Stream.of(
                Arguments.of("   ", null, "empty line"),
                Arguments.of("a;b;c;d", null, "expected 5 fields, got 4"),
                Arguments.of(";Монітори;1;1;1", "name", "empty field"),
                Arguments.of("Монітор;;1;1;1", "category", "empty field"),
                Arguments.of("Монітор;Монітори;абв;1;1", "price", "not a number: \"абв\""),
                Arguments.of("Монітор;Монітори;0;1;1", "price", "must be greater than zero: \"0\""),
                Arguments.of(
                        "Монітор;Монітори;1;12.5;1",
                        "warrantyMonths",
                        "must be an integer: \"12.5\""),
                Arguments.of("Монітор;Монітори;1;1;-3", "stock", "negative value: \"-3\""),
                Arguments.of(
                        "Монітор;Монітори;1;1;2147483648",
                        "stock",
                        "value is too large: \"2147483648\""));
    }

    @ParameterizedTest
    @MethodSource("englishReasons")
    void rendersEnglishReasonForTheLog(String line, String expectedField, String expectedReason) {
        ProductValidationException e = rejected(line);

        assertEquals(expectedField, e.field() == null ? null : e.field().csvName());
        assertEquals(expectedReason, e.error().english(e.detail()));
    }
}
