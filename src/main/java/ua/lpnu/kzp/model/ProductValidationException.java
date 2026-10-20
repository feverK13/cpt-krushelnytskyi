package ua.lpnu.kzp.model;

import java.io.Serial;

/**
 * Thrown when a value violates an invariant of {@link Product} or {@link WarrantyStock}.
 *
 * <p>The exception carries the offending {@link RecordField}, the {@link ValidationError} reason
 * and, for reasons that quote a value, that value in canonical form (never the raw CSV text, which
 * the model does not see). A caller that holds the raw text, such as the CSV factory, renders the
 * user-facing message itself from {@link #error()} and the raw text.</p>
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
     * @param field  the offending field
     * @param error  the reason the value was rejected
     * @param detail the offending value in canonical form, or {@code null} if the reason has none
     */
    public ProductValidationException(RecordField field, ValidationError error, String detail) {
        super(field.csvName() + ": " + error.english(detail));
        this.field = field;
        this.error = error;
        this.detail = detail;
    }

    /**
     * Returns the offending field.
     *
     * @return the field
     */
    public RecordField field() {
        return field;
    }

    /**
     * Returns the reason the value was rejected.
     *
     * @return the reason
     */
    public ValidationError error() {
        return error;
    }

    /**
     * Returns the offending value in canonical form.
     *
     * @return the value formatted independently of the default locale, or {@code null} if the
     *         reason does not quote a value
     */
    public String detail() {
        return detail;
    }
}
