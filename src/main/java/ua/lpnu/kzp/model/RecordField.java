package ua.lpnu.kzp.model;

/**
 * The five fields of a record, in the order in which they appear in the CSV line.
 *
 * <p>Each field has a Ukrainian label for user messages and an English name for log records.</p>
 */
public enum RecordField {

    /** Product name. */
    NAME(0, "назва", "name"),

    /** Product category. */
    CATEGORY(1, "категорія", "category"),

    /** Product price, in UAH. */
    PRICE(2, "ціна", "price"),

    /** Warranty length, in months. */
    WARRANTY_MONTHS(3, "гарантія", "warrantyMonths"),

    /** Number of items in stock. */
    STOCK(4, "запас", "stock");

    private final int index;
    private final String label;
    private final String csvName;

    RecordField(int index, String label, String csvName) {
        this.index = index;
        this.label = label;
        this.csvName = csvName;
    }

    /** Returns the zero-based position of this field inside a record. */
    public int index() {
        return index;
    }

    /** Returns the Ukrainian label used in messages shown to the user. */
    public String label() {
        return label;
    }

    /** Returns the English field name used in log records. */
    public String csvName() {
        return csvName;
    }
}
