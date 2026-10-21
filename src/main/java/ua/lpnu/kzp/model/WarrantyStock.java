package ua.lpnu.kzp.model;

/**
 * The warranty length and the stock of a product as one immutable value.
 *
 * <p>Zero is valid for both components, negative is rejected. The checks live only here;
 * {@link Product} reuses them.</p>
 *
 * @param warrantyMonths warranty length in months, 0 or more
 * @param stock          number of items in stock, 0 or more
 */
public record WarrantyStock(int warrantyMonths, int stock) {

    /**
     * Validates both components, warranty first.
     *
     * @throws ProductValidationException if a component is negative
     */
    public WarrantyStock {
        checkWarrantyMonths(warrantyMonths);
        checkStock(stock);
    }

    static void checkWarrantyMonths(int warrantyMonths) {
        if (warrantyMonths < 0) {
            throw new ProductValidationException(
                    RecordField.WARRANTY_MONTHS, ValidationError.NEGATIVE, Integer.toString(warrantyMonths));
        }
    }

    static void checkStock(int stock) {
        if (stock < 0) {
            throw new ProductValidationException(
                    RecordField.STOCK, ValidationError.NEGATIVE, Integer.toString(stock));
        }
    }
}
