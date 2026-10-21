package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Product} constructor: valid values, boundaries, invalid values for
 * every field, the order in which fields are checked and {@code toString}.
 */
class ProductTest {

    private static final String NAME = "Ноутбук Lenovo IdeaPad 3";
    private static final String CATEGORY = "Ноутбуки";

    private static ProductValidationException reject(Executable constructorCall) {
        return assertThrows(ProductValidationException.class, constructorCall);
    }

    private static void assertRejected(
            ProductValidationException e, RecordField field, ValidationError error) {
        assertEquals(field, e.field());
        assertEquals(error, e.error());
    }

    @Test
    void storesAllFields() {
        String name = NAME;
        String category = CATEGORY;

        Product product = new Product(name, category, 24999.00, 24, 7);

        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(24999.00, product.getPrice());
        assertEquals(24, product.getWarrantyMonths());
        assertEquals(7, product.getStock());
    }

    @Test
    void trimsNameAndCategory() {
        Product product = new Product("  " + NAME + "\t", " \t" + CATEGORY + "  ", 1, 1, 1);

        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
    }

    @Test
    void exposesWarrantyAndStockAsRecord() {
        Product product = new Product(NAME, CATEGORY, 100, 24, 7);

        WarrantyStock value = product.warrantyStock();

        assertEquals(new WarrantyStock(24, 7), value);
    }

    @Test
    void rejectsNullName() {
        assertThrows(NullPointerException.class, () -> new Product(null, CATEGORY, 1, 1, 1));
    }

    @Test
    void rejectsNullCategory() {
        assertThrows(NullPointerException.class, () -> new Product(NAME, null, 1, 1, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t ", " "})
    void rejectsBlankName(String name) {
        ProductValidationException e = reject(() -> new Product(name, CATEGORY, 1, 1, 1));

        assertRejected(e, RecordField.NAME, ValidationError.EMPTY_FIELD);
        assertNull(e.detail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t ", " "})
    void rejectsBlankCategory(String category) {
        ProductValidationException e = reject(() -> new Product(NAME, category, 1, 1, 1));

        assertRejected(e, RecordField.CATEGORY, ValidationError.EMPTY_FIELD);
        assertNull(e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 1, 1.5, 24999.00, Double.MAX_VALUE, Double.MIN_VALUE})
    void acceptsPositivePrice(double price) {
        Product product = new Product(NAME, CATEGORY, price, 1, 1);

        assertEquals(price, product.getPrice());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.0})
    void rejectsZeroPriceAsNotPositive(double price) {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        assertRejected(e, RecordField.PRICE, ValidationError.NOT_POSITIVE);
        assertEquals("0.0", e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1, -24999.00, -Double.MAX_VALUE})
    void rejectsNegativePrice(double price) {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        assertRejected(e, RecordField.PRICE, ValidationError.NEGATIVE);
    }

    @Test
    void formatsNegativePriceDetailCanonically() {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, -0.01, 1, 1));

        assertEquals("-0.01", e.detail());
    }

    @Test
    void rejectsNaNPrice() {
        ProductValidationException e =
                reject(() -> new Product(NAME, CATEGORY, Double.NaN, 1, 1));

        assertRejected(e, RecordField.PRICE, ValidationError.NOT_A_NUMBER);
        assertEquals("NaN", e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInfinitePrice(double price) {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        assertRejected(e, RecordField.PRICE, ValidationError.TOO_LARGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 36, Integer.MAX_VALUE})
    void acceptsNonNegativeWarranty(int warranty) {
        Product product = new Product(NAME, CATEGORY, 1, warranty, 1);

        assertEquals(warranty, product.getWarrantyMonths());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -12, Integer.MIN_VALUE})
    void rejectsNegativeWarranty(int warranty) {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, warranty, 1));

        assertRejected(e, RecordField.WARRANTY_MONTHS, ValidationError.NEGATIVE);
        assertEquals(Integer.toString(warranty), e.detail());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 75, Integer.MAX_VALUE})
    void acceptsNonNegativeStock(int stock) {
        Product product = new Product(NAME, CATEGORY, 1, 1, stock);

        assertEquals(stock, product.getStock());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -3, Integer.MIN_VALUE})
    void rejectsNegativeStock(int stock) {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, 1, stock));

        assertRejected(e, RecordField.STOCK, ValidationError.NEGATIVE);
        assertEquals(Integer.toString(stock), e.detail());
    }

    @Test
    void acceptsZeroWarrantyAndZeroStockTogether() {
        Product product = new Product(NAME, CATEGORY, 0.01, 0, 0);

        assertEquals(new WarrantyStock(0, 0), product.warrantyStock());
    }

    @Test
    void blankNameWinsOverBadPrice() {
        ProductValidationException e = reject(() -> new Product(" ", CATEGORY, -5, 1, 1));

        assertRejected(e, RecordField.NAME, ValidationError.EMPTY_FIELD);
    }

    @Test
    void blankCategoryWinsOverBadPrice() {
        ProductValidationException e = reject(() -> new Product(NAME, "", 0, 1, 1));

        assertRejected(e, RecordField.CATEGORY, ValidationError.EMPTY_FIELD);
    }

    @Test
    void blankNameWinsOverBlankCategory() {
        ProductValidationException e = reject(() -> new Product("", "", 1, 1, 1));

        assertEquals(RecordField.NAME, e.field());
    }

    @Test
    void badPriceWinsOverBadWarrantyAndStock() {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 0, -1, -1));

        assertRejected(e, RecordField.PRICE, ValidationError.NOT_POSITIVE);
    }

    @Test
    void badWarrantyWinsOverBadStock() {
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, -1, -1));

        assertRejected(e, RecordField.WARRANTY_MONTHS, ValidationError.NEGATIVE);
    }

    @Test
    void everythingInvalidReportsName() {
        ProductValidationException e = reject(() -> new Product("", "", -1, -1, -1));

        assertEquals(RecordField.NAME, e.field());
    }

    @Test
    void toStringListsAllFieldsWithTwoDecimals() {
        Product product = new Product(NAME, CATEGORY, 24999, 24, 7);

        String text = product.toString();

        assertEquals(
                "Product[name=Ноутбук Lenovo IdeaPad 3, category=Ноутбуки, price=24999.00,"
                        + " warrantyMonths=24, stock=7]",
                text);
    }

    @Test
    void toStringIgnoresDefaultLocale() {
        Locale original = Locale.getDefault();
        Product product = new Product(NAME, CATEGORY, 1234.5, 12, 3);
        try {
            Locale.setDefault(Locale.GERMANY);

            String text = product.toString();

            assertEquals(
                    "Product[name=Ноутбук Lenovo IdeaPad 3, category=Ноутбуки, price=1234.50,"
                            + " warrantyMonths=12, stock=3]",
                    text);
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void validationDetailIgnoresDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, -1234.5, 1, 1));

            assertEquals("-1234.5", e.detail());
        } finally {
            Locale.setDefault(original);
        }
    }
}
