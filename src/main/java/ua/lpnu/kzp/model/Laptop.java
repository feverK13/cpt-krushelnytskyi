package ua.lpnu.kzp.model;

/** A laptop or notebook. */
public final class Laptop extends Product {

    Laptop(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.LAPTOP, name, category, price, warrantyMonths, stock);
    }
}
