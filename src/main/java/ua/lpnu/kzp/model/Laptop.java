package ua.lpnu.kzp.model;

/** A laptop or notebook. */
public final class Laptop extends Product {

    // Share of the price charged for each warranty year: laptops are the costliest to repair.
    /** Share of the price charged per warranty year. */
    public static final double RATE_PER_YEAR = 0.03;
    // Flat service fee in UAH for every started warranty year.
    /** Flat fee in UAH per started warranty year. */
    public static final double SURCHARGE_PER_STARTED_YEAR = 100.0;

    Laptop(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.LAPTOP, name, category, price, warrantyMonths, stock);
    }

    /** Returns 3% of the price per warranty year plus 100 UAH per started year. */
    @Override
    public double warrantyCost() {
        double years = getWarrantyMonths() / 12.0;
        return roundMoney(getPrice() * RATE_PER_YEAR * years
                + SURCHARGE_PER_STARTED_YEAR * Math.ceil(years));
    }
}
