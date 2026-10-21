package ua.lpnu.kzp.model;

import java.io.Serial;

/**
 * Thrown when a value violates an invariant of {@link Product} or {@link WarrantyStock}.
 *
 * <p>Carries the field, the reason and an optional value. The constructors of the model use a
 * canonical value; {@link Product#fromCsv} uses the raw field text, so the caller renders the
 * user-facing message from {@link #field()}, {@link #error()} and {@link #detail()}.</p>
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
     * @param field  the offending field, or {@code null} for a whole-line problem
     * @param detail the offending value (canonical, or raw text when thrown by
     *               {@link Product#fromCsv}), or {@code null} if the reason has none
     */
    public ProductValidationException(RecordField field, ValidationError error, String detail) {
        super(field == null ? error.english(detail) : field.csvName() + ": " + error.english(detail));
        this.field = field;
        this.error = error;
        this.detail = detail;
    }

    /** Returns the offending field, or {@code null} for a whole-line problem. */
    public RecordField field() {
        return field;
    }

    /** Returns the reason the value was rejected. */
    public ValidationError error() {
        return error;
    }

    /** Returns the offending value, or {@code null} if the reason has none. */
    public String detail() {
        return detail;
    }
}
