package ua.lpnu.kzp.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * An immutable, always valid item of the electronics store.
 *
 * <p>Price must be {@code > 0}: this is a compliant refinement of the Lab 2 minimum rule, because
 * Lab 1 rejected a price of 0.</p>
 */
public final class Product {

    private final String name;
    private final String category;
    private final double price;
    private final int warrantyMonths;
    private final int stock;

    /**
     * Creates a product; name and category are stripped.
     *
     * @throws NullPointerException       if {@code name} or {@code category} is {@code null}
     * @throws ProductValidationException for the first failing field, checked in the order of the
     *                                    parameters (the Lab 1 order)
     */
    public Product(String name, String category, double price, int warrantyMonths, int stock) {
        this.name = checkName(name);
        this.category = checkCategory(category);
        checkPrice(price);
        this.price = price;
        WarrantyStock.checkWarrantyMonths(warrantyMonths);
        this.warrantyMonths = warrantyMonths;
        WarrantyStock.checkStock(stock);
        this.stock = stock;
    }

    // null is a programming error, not bad input, so it is not a ProductValidationException.
    static String checkName(String name) {
        return checkText(RecordField.NAME, Objects.requireNonNull(name, "name"));
    }

    static String checkCategory(String category) {
        return checkText(RecordField.CATEGORY, Objects.requireNonNull(category, "category"));
    }

    private static String checkText(RecordField field, String value) {
        String stripped = value.strip();
        if (stripped.isEmpty()) {
            throw new ProductValidationException(field, ValidationError.EMPTY_FIELD, null);
        }
        return stripped;
    }

    // -0.0 is not less than 0, so it is reported as NOT_POSITIVE, like "-0" in Lab 1.
    static void checkPrice(double price) {
        if (Double.isNaN(price)) {
            throw new ProductValidationException(
                    RecordField.PRICE, ValidationError.NOT_A_NUMBER, Double.toString(price));
        }
        if (Double.isInfinite(price)) {
            throw new ProductValidationException(
                    RecordField.PRICE, ValidationError.TOO_LARGE, Double.toString(price));
        }
        if (price < 0) {
            throw new ProductValidationException(
                    RecordField.PRICE, ValidationError.NEGATIVE, canonical(price));
        }
        if (price <= 0) {
            throw new ProductValidationException(
                    RecordField.PRICE, ValidationError.NOT_POSITIVE, canonical(price));
        }
    }

    private static String canonical(double price) {
        return BigDecimal.valueOf(price).toPlainString();
    }

    /** Returns the stripped, non-blank name. */
    public String getName() {
        return name;
    }

    /** Returns the stripped, non-blank category. */
    public String getCategory() {
        return category;
    }

    /** Returns the price in UAH. */
    public double getPrice() {
        return price;
    }

    /** Returns the warranty length in months. */
    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    /** Returns the number of items in stock. */
    public int getStock() {
        return stock;
    }

    /** Returns the warranty and the stock as one immutable value. */
    public WarrantyStock warrantyStock() {
        return new WarrantyStock(warrantyMonths, stock);
    }

    /** Renders the product for diagnostics, with a locale-independent two-decimal price. */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "Product[name=%s, category=%s, price=%.2f, warrantyMonths=%d, stock=%d]",
                name,
                category,
                price,
                warrantyMonths,
                stock);
    }
}
