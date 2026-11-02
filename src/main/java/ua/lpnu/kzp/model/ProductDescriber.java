package ua.lpnu.kzp.model;

import java.util.Locale;

/**
 * Describes the warranty policy of a product in words.
 *
 * <p>This is presentation logic and lives outside the domain classes: {@link
 * Product#warrantyCost()} itself stays polymorphic and has no {@code switch}. The {@code switch}
 * below has no {@code default} branch, so adding a new subtype to the sealed {@link Product}
 * without updating it is a compile error.</p>
 */
public final class ProductDescriber {

    private ProductDescriber() {
    }

    /**
     * Returns the Ukrainian description of the warranty policy of the product's subtype, built
     * from the rate constants of that subtype.
     *
     * @param product the product to describe
     * @return e.g. {@code 3% ціни за кожен рік гарантії + 100 грн за кожен розпочатий рік}
     */
    public static String describe(Product product) {
        return switch (product) {
            case Laptop laptop -> String.format(
                    Locale.ROOT, "%s + %.0f грн за кожен розпочатий рік",
                    percentPerYear(Laptop.RATE_PER_YEAR), Laptop.SURCHARGE_PER_STARTED_YEAR);
            case Peripheral peripheral -> percentPerYear(Peripheral.RATE_PER_YEAR);
            case OtherProduct other -> percentPerYear(OtherProduct.RATE_PER_YEAR);
        };
    }

    private static String percentPerYear(double rate) {
        return String.format(Locale.ROOT, "%.0f%% ціни за кожен рік гарантії", rate * 100);
    }
}
