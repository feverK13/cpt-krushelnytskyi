package ua.lpnu.kzp.model;

/** A computer peripheral or accessory. */
public final class Peripheral extends Product {

    // Share of the price charged for each warranty year: peripherals are cheap to service.
    /** Share of the price charged per warranty year. */
    public static final double RATE_PER_YEAR = 0.01;

    Peripheral(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.PERIPHERAL, name, category, price, warrantyMonths, stock);
    }

    /** Returns 1% of the price per warranty year. */
    @Override
    public double warrantyCost() {
        return roundMoney(getPrice() * RATE_PER_YEAR * (getWarrantyMonths() / 12.0));
    }
}
