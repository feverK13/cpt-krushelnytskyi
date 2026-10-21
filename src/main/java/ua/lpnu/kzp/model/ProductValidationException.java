package ua.lpnu.kzp.model;

import java.io.Serial;

/**
 * Thrown when a value violates an invariant of {@link Product} or {@link WarrantyStock}.
 *
 * <p>Carries the field, the reason and an optional canonical value. It never holds the raw CSV
 * text, so a caller that has it renders the user-facing message from {@link #error()} itself.</p>
 */
public final class ProductValidationException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final RecordField field;
    private final ValidationError error;
    private final String detail;

    /**
     * Creates an exception for a single failed check.
     *
     * @param detail the offending value in canonical form, or {@code null} if the reason has none
     */
    public ProductValidationException(RecordField field, ValidationError error, String detail) {
        super(field.csvName() + ": " + error.english(detail));
        this.field = field;
        this.error = error;
        this.detail = detail;
    }

    /** Returns the offending field. */
    public RecordField field() {
        return field;
    }

    /** Returns the reason the value was rejected. */
    public ValidationError error() {
        return error;
    }

    /** Returns the offending value in canonical form, or {@code null} if the reason has none. */
    public String detail() {
        return detail;
    }
}
