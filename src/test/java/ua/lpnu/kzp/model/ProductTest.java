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
        // Arrange
        String name = NAME;
        String category = CATEGORY;

        // Act
        Product product = new Product(name, category, 24999.00, 24, 7);

        // Assert
        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(24999.00, product.getPrice());
        assertEquals(24, product.getWarrantyMonths());
        assertEquals(7, product.getStock());
    }

    @Test
    void trimsNameAndCategory() {
        // Arrange and Act
        Product product = new Product("  " + NAME + "\t", " \t" + CATEGORY + "  ", 1, 1, 1);

        // Assert
        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
    }

    @Test
    void exposesWarrantyAndStockAsRecord() {
        // Arrange
        Product product = new Product(NAME, CATEGORY, 100, 24, 7);

        // Act
        WarrantyStock value = product.warrantyStock();

        // Assert
        assertEquals(new WarrantyStock(24, 7), value);
    }

    @Test
    void rejectsNullName() {
        // Arrange and Act and Assert
        assertThrows(NullPointerException.class, () -> new Product(null, CATEGORY, 1, 1, 1));
    }

    @Test
    void rejectsNullCategory() {
        // Arrange and Act and Assert
        assertThrows(NullPointerException.class, () -> new Product(NAME, null, 1, 1, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t ", " "})
    void rejectsBlankName(String name) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(name, CATEGORY, 1, 1, 1));

        // Assert
        assertRejected(e, RecordField.NAME, ValidationError.EMPTY_FIELD);
        assertNull(e.detail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t ", " "})
    void rejectsBlankCategory(String category) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, category, 1, 1, 1));

        // Assert
        assertRejected(e, RecordField.CATEGORY, ValidationError.EMPTY_FIELD);
        assertNull(e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 1, 1.5, 24999.00, Double.MAX_VALUE, Double.MIN_VALUE})
    void acceptsPositivePrice(double price) {
        // Arrange and Act
        Product product = new Product(NAME, CATEGORY, price, 1, 1);

        // Assert
        assertEquals(price, product.getPrice());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.0})
    void rejectsZeroPriceAsNotPositive(double price) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        // Assert
        assertRejected(e, RecordField.PRICE, ValidationError.NOT_POSITIVE);
        assertEquals("0.0", e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1, -24999.00, -Double.MAX_VALUE})
    void rejectsNegativePrice(double price) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        // Assert
        assertRejected(e, RecordField.PRICE, ValidationError.NEGATIVE);
    }

    @Test
    void formatsNegativePriceDetailCanonically() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, -0.01, 1, 1));

        // Assert
        assertEquals("-0.01", e.detail());
    }

    @Test
    void rejectsNaNPrice() {
        // Arrange and Act
        ProductValidationException e =
                reject(() -> new Product(NAME, CATEGORY, Double.NaN, 1, 1));

        // Assert
        assertRejected(e, RecordField.PRICE, ValidationError.NOT_A_NUMBER);
        assertEquals("NaN", e.detail());
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInfinitePrice(double price) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, price, 1, 1));

        // Assert
        assertRejected(e, RecordField.PRICE, ValidationError.TOO_LARGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 36, Integer.MAX_VALUE})
    void acceptsNonNegativeWarranty(int warranty) {
        // Arrange and Act
        Product product = new Product(NAME, CATEGORY, 1, warranty, 1);

        // Assert
        assertEquals(warranty, product.getWarrantyMonths());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -12, Integer.MIN_VALUE})
    void rejectsNegativeWarranty(int warranty) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, warranty, 1));

        // Assert
        assertRejected(e, RecordField.WARRANTY_MONTHS, ValidationError.NEGATIVE);
        assertEquals(Integer.toString(warranty), e.detail());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 75, Integer.MAX_VALUE})
    void acceptsNonNegativeStock(int stock) {
        // Arrange and Act
        Product product = new Product(NAME, CATEGORY, 1, 1, stock);

        // Assert
        assertEquals(stock, product.getStock());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -3, Integer.MIN_VALUE})
    void rejectsNegativeStock(int stock) {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, 1, stock));

        // Assert
        assertRejected(e, RecordField.STOCK, ValidationError.NEGATIVE);
        assertEquals(Integer.toString(stock), e.detail());
    }

    @Test
    void acceptsZeroWarrantyAndZeroStockTogether() {
        // Arrange and Act
        Product product = new Product(NAME, CATEGORY, 0.01, 0, 0);

        // Assert
        assertEquals(new WarrantyStock(0, 0), product.warrantyStock());
    }

    @Test
    void blankNameWinsOverBadPrice() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(" ", CATEGORY, -5, 1, 1));

        // Assert
        assertRejected(e, RecordField.NAME, ValidationError.EMPTY_FIELD);
    }

    @Test
    void blankCategoryWinsOverBadPrice() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, "", 0, 1, 1));

        // Assert
        assertRejected(e, RecordField.CATEGORY, ValidationError.EMPTY_FIELD);
    }

    @Test
    void blankNameWinsOverBlankCategory() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product("", "", 1, 1, 1));

        // Assert
        assertEquals(RecordField.NAME, e.field());
    }

    @Test
    void badPriceWinsOverBadWarrantyAndStock() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 0, -1, -1));

        // Assert
        assertRejected(e, RecordField.PRICE, ValidationError.NOT_POSITIVE);
    }

    @Test
    void badWarrantyWinsOverBadStock() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, 1, -1, -1));

        // Assert
        assertRejected(e, RecordField.WARRANTY_MONTHS, ValidationError.NEGATIVE);
    }

    @Test
    void everythingInvalidReportsName() {
        // Arrange and Act
        ProductValidationException e = reject(() -> new Product("", "", -1, -1, -1));

        // Assert
        assertEquals(RecordField.NAME, e.field());
    }

    @Test
    void toStringListsAllFieldsWithTwoDecimals() {
        // Arrange
        Product product = new Product(NAME, CATEGORY, 24999, 24, 7);

        // Act
        String text = product.toString();

        // Assert
        assertEquals(
                "Product[name=Ноутбук Lenovo IdeaPad 3, category=Ноутбуки, price=24999.00,"
                        + " warrantyMonths=24, stock=7]",
                text);
    }

    @Test
    void toStringIgnoresDefaultLocale() {
        // Arrange
        Locale original = Locale.getDefault();
        Product product = new Product(NAME, CATEGORY, 1234.5, 12, 3);
        try {
            Locale.setDefault(Locale.GERMANY);

            // Act
            String text = product.toString();

            // Assert
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
        // Arrange
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            // Act
            ProductValidationException e = reject(() -> new Product(NAME, CATEGORY, -1234.5, 1, 1));

            // Assert
            assertEquals("-1234.5", e.detail());
        } finally {
            Locale.setDefault(original);
        }
    }
}
