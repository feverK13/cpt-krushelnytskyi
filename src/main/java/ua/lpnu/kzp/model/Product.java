package ua.lpnu.kzp.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * An item of the electronics store: {@code name;category;price;warrantyMonths;stock}.
 *
 * <p>The object is immutable and always valid: every invariant is checked in the constructor, so
 * an invalid {@code Product} cannot exist. The rules and their order are those of Lab 1; the first
 * failing check wins:</p>
 * <ol>
 *   <li>{@code name} is not blank,</li>
 *   <li>{@code category} is not blank,</li>
 *   <li>{@code price} is finite and greater than 0,</li>
 *   <li>{@code warrantyMonths} is 0 or more,</li>
 *   <li>{@code stock} is 0 or more.</li>
 * </ol>
 *
 * <p>The price rule is {@code > 0}, not {@code >= 0}. This is a compliant refinement of the
 * Lab 2 minimum rules by the Lab 1 rules: zero is allowed only where Lab 1 allowed it, and Lab 1
 * rejected a price of 0 (allowing it would also change the average price). Warranty and stock
 * allow 0.</p>
 */
public final class Product {

    private final String name;
    private final String category;
    private final double price;
    private final int warrantyMonths;
    private final int stock;

    /**
     * Creates a product after checking every invariant.
     *
     * <p>Name and category are stripped of surrounding whitespace. A non-finite price is rejected
     * here for direct callers; the CSV parsing layer reports such values as parse errors before
     * they reach the constructor.</p>
     *
     * @param name           product name, not blank
     * @param category       product category, not blank
     * @param price          price in UAH, finite and greater than 0
     * @param warrantyMonths warranty length in months, 0 or more
     * @param stock          number of items in stock, 0 or more
     * @throws NullPointerException       if {@code name} or {@code category} is {@code null}
     * @throws ProductValidationException if an invariant is violated; the exception names the
     *                                    first failing field in the order given above
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

    /**
     * Checks a product name.
     *
     * @param name the name, possibly padded with whitespace
     * @return the stripped name
     * @throws NullPointerException       if {@code name} is {@code null}
     * @throws ProductValidationException if the stripped name is empty
     */
    static String checkName(String name) {
        return checkText(RecordField.NAME, Objects.requireNonNull(name, "name"));
    }

    /**
     * Checks a product category.
     *
     * @param category the category, possibly padded with whitespace
     * @return the stripped category
     * @throws NullPointerException       if {@code category} is {@code null}
     * @throws ProductValidationException if the stripped category is empty
     */
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

    /**
     * Checks a price: finite and strictly positive. {@code -0.0} is rejected as not positive, not
     * as negative, like {@code -0} in Lab 1.
     *
     * @param price the price to check
     * @throws ProductValidationException if the price is NaN, infinite, negative or zero
     */
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

    /** Formats a finite price without exponent and independently of the default locale. */
    private static String canonical(double price) {
        return BigDecimal.valueOf(price).toPlainString();
    }

    /**
     * Returns the product name.
     *
     * @return the stripped, non-blank name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the product category.
     *
     * @return the stripped, non-blank category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the price.
     *
     * @return the price in UAH, finite and greater than 0
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the warranty length.
     *
     * @return warranty in months, 0 or more
     */
    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    /**
     * Returns the stock quantity.
     *
     * @return number of items in stock, 0 or more
     */
    public int getStock() {
        return stock;
    }

    /**
     * Returns the warranty and the stock as one immutable value.
     *
     * @return a new {@link WarrantyStock} equal to this product's warranty and stock
     */
    public WarrantyStock warrantyStock() {
        return new WarrantyStock(warrantyMonths, stock);
    }

    /**
     * Renders the product for diagnostics, with the price in two decimals independently of the
     * default locale.
     *
     * @return e.g. {@code Product[name=Монітор, category=Монітори, price=6499.00, warrantyMonths=36, stock=12]}
     */
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
