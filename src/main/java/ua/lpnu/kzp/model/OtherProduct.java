package ua.lpnu.kzp.model;

/** Any product that is neither a laptop nor a peripheral. */
public final class OtherProduct extends Product {

    OtherProduct(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.OTHER, name, category, price, warrantyMonths, stock);
    }
}
