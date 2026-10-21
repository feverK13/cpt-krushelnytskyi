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
        ProductValidationException e =
                new ProductValidationException(RecordField.STOCK, ValidationError.NEGATIVE, "-3");

        assertEquals(RecordField.STOCK, e.field());
        assertEquals(ValidationError.NEGATIVE, e.error());
        assertEquals("-3", e.detail());
    }

    @Test
    void isAnIllegalArgumentException() {
        ProductValidationException e =
                new ProductValidationException(RecordField.NAME, ValidationError.EMPTY_FIELD, null);

        assertInstanceOf(IllegalArgumentException.class, e);
        assertNull(e.detail());
    }

    @Test
    void messageNamesFieldAndEnglishReasonWithDetail() {
        ProductValidationException e = new ProductValidationException(
                RecordField.PRICE, ValidationError.NOT_POSITIVE, "0.0");

        assertEquals("price: must be greater than zero: \"0.0\"", e.getMessage());
    }

    @Test
    void messageWithoutDetailHasNoQuotedValue() {
        ProductValidationException e =
                new ProductValidationException(RecordField.CATEGORY, ValidationError.EMPTY_FIELD, null);

        assertEquals("category: empty field", e.getMessage());
    }
}
