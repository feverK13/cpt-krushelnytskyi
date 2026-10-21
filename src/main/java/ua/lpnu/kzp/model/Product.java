package ua.lpnu.kzp.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An immutable, always valid item of the electronics store.
 *
 * <p>Price must be {@code > 0}: this is a compliant refinement of the Lab 2 minimum rule, because
 * Lab 1 rejected a price of 0.</p>
 */
public final class Product {

    private static final int FIELD_COUNT = 5;
    private static final String SEPARATOR = ";";
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);

    private final String name;
    private final String category;
    private final double price;
    private final int warrantyMonths;
    private final int stock;

    /**
     * Creates a product; name and category are stripped.
     *
     * <p>Non-finite prices are rejected here for direct callers, while {@link #fromCsv} reports a
     * price that overflows {@code double} as {@link ValidationError#TOO_LARGE}.</p>
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

    /**
     * Returns a new builder.
     *
     * @return an empty {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parses one CSV line {@code name;category;price;warrantyMonths;stock}; fields are trimmed.
     *
     * <p>Numbers are plain decimals ({@code -?\d+(\.\d+)?}); warranty and stock must fit into
     * {@code int}. Each field is parsed and checked before the next one, so the first failing
     * field wins.</p>
     *
     * @throws NullPointerException       if {@code line} is {@code null}
     * @throws ProductValidationException for a blank line, a wrong field count or the first
     *                                    invalid field; its {@link ProductValidationException#detail()
     *                                    detail} is the raw field text, so the Lab 1 message can be
     *                                    rendered from {@code field()}, {@code error()} and
     *                                    {@code detail()}
     */
    public static Product fromCsv(String line) {
        Objects.requireNonNull(line, "line");
        if (line.isBlank()) {
            throw new ProductValidationException(null, ValidationError.EMPTY_LINE, null);
        }
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            throw new ProductValidationException(
                    null, ValidationError.FIELD_COUNT, String.valueOf(fields.length));
        }
        String name = checkName(fields[RecordField.NAME.index()]);
        String category = checkCategory(fields[RecordField.CATEGORY.index()]);
        double price = parsePrice(fields[RecordField.PRICE.index()].strip());
        int warrantyMonths = parseInt(
                RecordField.WARRANTY_MONTHS, fields[RecordField.WARRANTY_MONTHS.index()].strip());
        int stock = parseInt(RecordField.STOCK, fields[RecordField.STOCK.index()].strip());
        return new Product(name, category, price, warrantyMonths, stock);
    }

    private static double parsePrice(String raw) {
        requirePresent(RecordField.PRICE, raw);
        if (!DECIMAL.matcher(raw).matches()) {
            throw new ProductValidationException(RecordField.PRICE, ValidationError.NOT_A_NUMBER, raw);
        }
        double price = Double.parseDouble(raw);
        if (Double.isInfinite(price)) {
            throw new ProductValidationException(RecordField.PRICE, ValidationError.TOO_LARGE, raw);
        }
        try {
            checkPrice(price);
        } catch (ProductValidationException e) {
            throw withRawDetail(e, raw);
        }
        return price;
    }

    private static int parseInt(RecordField field, String raw) {
        requirePresent(field, raw);
        if (!INTEGER.matcher(raw).matches()) {
            throw new ProductValidationException(
                    field,
                    DECIMAL.matcher(raw).matches() ? ValidationError.NOT_AN_INTEGER : ValidationError.NOT_A_NUMBER,
                    raw);
        }
        BigInteger parsed = new BigInteger(raw);
        if (parsed.compareTo(MAX_INT) > 0) {
            throw new ProductValidationException(field, ValidationError.TOO_LARGE, raw);
        }
        // Below Integer.MIN_VALUE the value is clamped so the field check still reports NEGATIVE.
        int value = parsed.max(MIN_INT).intValue();
        try {
            if (field == RecordField.WARRANTY_MONTHS) {
                WarrantyStock.checkWarrantyMonths(value);
            } else {
                WarrantyStock.checkStock(value);
            }
        } catch (ProductValidationException e) {
            throw withRawDetail(e, raw);
        }
        return value;
    }

    private static void requirePresent(RecordField field, String raw) {
        if (raw.isEmpty()) {
            throw new ProductValidationException(field, ValidationError.EMPTY_FIELD, null);
        }
    }

    // Lab 1 quotes the field as written ("0.00", "-03"), not in canonical form.
    private static ProductValidationException withRawDetail(ProductValidationException e, String raw) {
        return new ProductValidationException(e.field(), e.error(), raw);
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

    /**
     * Collects field values and creates a {@link Product} through its validating constructor, so
     * the builder cannot produce an invalid product. Unset text is {@code null}, unset numbers
     * are 0.
     */
    public static final class Builder {

        private String name;
        private String category;
        private double price;
        private int warrantyMonths;
        private int stock;

        private Builder() {
        }

        /** Sets the name. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Sets the category. */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /** Sets the price in UAH. */
        public Builder price(double price) {
            this.price = price;
            return this;
        }

        /** Sets the warranty length in months. */
        public Builder warrantyMonths(int warrantyMonths) {
            this.warrantyMonths = warrantyMonths;
            return this;
        }

        /** Sets the number of items in stock. */
        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        /**
         * Creates a new product from the current values.
         *
         * @throws NullPointerException       if the name or the category is not set
         * @throws ProductValidationException as the {@link Product} constructor does
         */
        public Product build() {
            return new Product(name, category, price, warrantyMonths, stock);
        }
    }
}
