package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ProductValidationException}: carried data and the English message.
 */
class ProductValidationExceptionTest {

    @Test
    void carriesFieldErrorAndDetail() {
        // Arrange and Act
        ProductValidationException e =
                new ProductValidationException(RecordField.STOCK, ValidationError.NEGATIVE, "-3");

        // Assert
        assertEquals(RecordField.STOCK, e.field());
        assertEquals(ValidationError.NEGATIVE, e.error());
        assertEquals("-3", e.detail());
    }

    @Test
    void isAnIllegalArgumentException() {
        // Arrange and Act
        ProductValidationException e =
                new ProductValidationException(RecordField.NAME, ValidationError.EMPTY_FIELD, null);

        // Assert
        assertInstanceOf(IllegalArgumentException.class, e);
        assertNull(e.detail());
    }

    @Test
    void messageNamesFieldAndEnglishReasonWithDetail() {
        // Arrange and Act
        ProductValidationException e = new ProductValidationException(
                RecordField.PRICE, ValidationError.NOT_POSITIVE, "0.0");

        // Assert
        assertEquals("price: must be greater than zero: \"0.0\"", e.getMessage());
    }

    @Test
    void messageWithoutDetailHasNoQuotedValue() {
        // Arrange and Act
        ProductValidationException e =
                new ProductValidationException(RecordField.CATEGORY, ValidationError.EMPTY_FIELD, null);

        // Assert
        assertEquals("category: empty field", e.getMessage());
    }
}
