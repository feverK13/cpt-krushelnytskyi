package ua.lpnu.kzp.model;

import java.util.Locale;
import java.util.Set;

/**
 * Kind of a product, derived from its category.
 *
 * <p>Categories are matched after trimming and lower-casing:</p>
 * <ul>
 *   <li>{@link #LAPTOP}: ноутбуки, ноутбук, laptops, laptop, notebooks, notebook;</li>
 *   <li>{@link #PERIPHERAL}: аксесуари, периферія, монітори, монітор, клавіатури, миші, мишки,
 *       навушники, принтери, аудіо, accessories, peripherals, monitors, keyboards, mice,
 *       headphones, printers, audio;</li>
 *   <li>{@link #OTHER}: everything else, including {@code null} and blank text.</li>
 * </ul>
 */
public enum ProductKind {

    /** Laptops and notebooks. */
    LAPTOP("Ноутбук", Set.of("ноутбуки", "ноутбук", "laptops", "laptop", "notebooks", "notebook"),
            Laptop::new),

    /** Computer peripherals and accessories. */
    PERIPHERAL("Периферія", Set.of(
            "аксесуари", "периферія", "монітори", "монітор", "клавіатури", "миші", "мишки",
            "навушники", "принтери", "аудіо", "accessories", "peripherals", "monitors",
            "keyboards", "mice", "headphones", "printers", "audio"),
            Peripheral::new),

    /** Any other product; the fallback kind. */
    OTHER("Інше", Set.of(), OtherProduct::new);

    /** Builds the subtype of one kind from raw field values. */
    @FunctionalInterface
    public interface Creator {

        /** Creates a product; validation is done by the {@link Product} constructor. */
        Product create(String name, String category, double price, int warrantyMonths, int stock);
    }

    private final String label;
    private final Set<String> categories;
    private final Creator creator;

    ProductKind(String label, Set<String> categories, Creator creator) {
        this.label = label;
        this.categories = Set.copyOf(categories);
        this.creator = creator;
    }

    /** Returns the human-readable label. */
    public String label() {
        return label;
    }

    /** Returns the lower-case CSV category names of this kind; empty for {@link #OTHER}. */
    public Set<String> categories() {
        return Set.copyOf(categories);
    }

    /**
     * Returns the kind of a category; never throws.
     *
     * @param category raw category text, may be {@code null}
     * @return the matching kind, {@link #OTHER} for {@code null}, blank or unknown text
     */
    public static ProductKind fromCategory(String category) {
        if (category == null) {
            return OTHER;
        }
        String key = category.strip().toLowerCase(Locale.ROOT);
        for (ProductKind kind : values()) {
            if (kind.categories.contains(key)) {
                return kind;
            }
        }
        return OTHER;
    }

    /** Returns {@code true} if {@link #fromCategory} maps the category to this kind. */
    public boolean accepts(String category) {
        return fromCategory(category) == this;
    }

    /**
     * Creates the subtype of this kind.
     *
     * @throws NullPointerException       if {@code name} or {@code category} is {@code null}
     * @throws ProductValidationException for the first failing field
     */
    public Product create(String name, String category, double price, int warrantyMonths, int stock) {
        return creator.create(name, category, price, warrantyMonths, stock);
    }
}
