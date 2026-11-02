package ua.lpnu.kzp.model;

/** Any product that is neither a laptop nor a peripheral. */
public final class OtherProduct extends Product {

    // Share of the price charged for each warranty year for all other products.
    private static final double RATE_PER_YEAR = 0.02;

    OtherProduct(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.OTHER, name, category, price, warrantyMonths, stock);
    }

    /** Returns 2% of the price per warranty year. */
    @Override
    public double warrantyCost() {
        return roundMoney(getPrice() * RATE_PER_YEAR * (getWarrantyMonths() / 12.0));
    }
}
