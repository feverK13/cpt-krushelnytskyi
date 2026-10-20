package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link WarrantyStock}: valid values, boundaries, invalid values and value
 * semantics of the record.
 */
class WarrantyStockTest {

    @Test
    void storesBothComponents() {
        // Arrange
        int warranty = 24;
        int stock = 7;

        // Act
        WarrantyStock value = new WarrantyStock(warranty, stock);

        // Assert
        assertEquals(24, value.warrantyMonths());
        assertEquals(7, value.stock());
    }

    @Test
    void acceptsZeroForBothComponents() {
        // Arrange and Act
        WarrantyStock value = new WarrantyStock(0, 0);

        // Assert
        assertEquals(0, value.warrantyMonths());
        assertEquals(0, value.stock());
    }

    @Test
    void acceptsIntegerMaxValue() {
        // Arrange and Act
        WarrantyStock value = new WarrantyStock(Integer.MAX_VALUE, Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, value.warrantyMonths());
        assertEquals(Integer.MAX_VALUE, value.stock());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -36, Integer.MIN_VALUE})
    void rejectsNegativeWarranty(int warranty) {
        // Arrange and Act
        ProductValidationException e =
                assertThrows(ProductValidationException.class, () -> new WarrantyStock(warranty, 1));

        // Assert
        assertEquals(RecordField.WARRANTY_MONTHS, e.field());
        assertEquals(ValidationError.NEGATIVE, e.error());
        assertEquals(Integer.toString(warranty), e.detail());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -75, Integer.MIN_VALUE})
    void rejectsNegativeStock(int stock) {
        // Arrange and Act
        ProductValidationException e =
                assertThrows(ProductValidationException.class, () -> new WarrantyStock(1, stock));

        // Assert
        assertEquals(RecordField.STOCK, e.field());
        assertEquals(ValidationError.NEGATIVE, e.error());
        assertEquals(Integer.toString(stock), e.detail());
    }

    @Test
    void reportsWarrantyFirstWhenBothComponentsAreInvalid() {
        // Arrange and Act
        ProductValidationException e =
                assertThrows(ProductValidationException.class, () -> new WarrantyStock(-1, -2));

        // Assert
        assertEquals(RecordField.WARRANTY_MONTHS, e.field());
        assertEquals("-1", e.detail());
    }

    @Test
    void rejectionIsAnIllegalArgumentException() {
        // Arrange and Act
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> new WarrantyStock(-1, 0));

        // Assert
        assertEquals(ProductValidationException.class, e.getClass());
    }

    @Test
    void equalValuesAreEqualAndShareHashCode() {
        // Arrange
        WarrantyStock first = new WarrantyStock(12, 3);
        WarrantyStock second = new WarrantyStock(12, 3);

        // Act and Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void differentValuesAreNotEqual() {
        // Arrange
        WarrantyStock base = new WarrantyStock(12, 3);

        // Act and Assert
        assertNotEquals(base, new WarrantyStock(13, 3));
        assertNotEquals(base, new WarrantyStock(12, 4));
        assertNotEquals(base, new WarrantyStock(3, 12));
        assertNotEquals(null, base);
    }

    @Test
    void toStringListsBothComponents() {
        // Arrange
        WarrantyStock value = new WarrantyStock(24, 7);

        // Act
        String text = value.toString();

        // Assert
        assertEquals("WarrantyStock[warrantyMonths=24, stock=7]", text);
    }
}
