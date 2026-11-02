package ua.lpnu.kzp.model;

/** A computer peripheral or accessory. */
public final class Peripheral extends Product {

    Peripheral(String name, String category, double price, int warrantyMonths, int stock) {
        super(ProductKind.PERIPHERAL, name, category, price, warrantyMonths, stock);
    }
}
