package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of the polymorphic {@link Product#warrantyCost()} with hand-calculated values. */
class WarrantyCostTest {

    private static final double EPS = 1e-9;

    private static Product laptop(double price, int months) {
        return Product.of("Ноутбук", "Ноутбуки", price, months, 1);
    }

    private static Product peripheral(double price, int months) {
        return Product.of("Миша", "Аксесуари", price, months, 1);
    }

    private static Product other(double price, int months) {
        return Product.of("Телевізор", "Телевізори", price, months, 1);
    }

    @Test
    void laptopTwoYears() {
        // 40000 * 0.03 * 2 = 2400, plus 100 * 2 started years
        assertEquals(2600.00, laptop(40000, 24).warrantyCost(), EPS);
    }

    @Test
    void laptopEighteenMonthsCountsTwoStartedYears() {
        // 10000 * 0.03 * 1.5 = 450, plus 100 * ceil(1.5) = 200
        assertEquals(650.00, laptop(10000, 18).warrantyCost(), EPS);
    }

    @Test
    void laptopOneMonthCountsOneStartedYear() {
        // 12000 * 0.03 * (1/12) = 30, plus 100
        assertEquals(130.00, laptop(12000, 1).warrantyCost(), EPS);
    }

    @Test
    void peripheralOneYear() {
        assertEquals(15.00, peripheral(1500, 12).warrantyCost(), EPS);
    }

    @Test
    void otherHalfYear() {
        assertEquals(10.00, other(1000, 6).warrantyCost(), EPS);
    }

    @Test
    void zeroMonthsCostNothing() {
        assertEquals(0.0, laptop(40000, 0).warrantyCost(), EPS);
        assertEquals(0.0, peripheral(1500, 0).warrantyCost(), EPS);
        assertEquals(0.0, other(1000, 0).warrantyCost(), EPS);
    }

    @Test
    void resultIsRoundedToTwoDecimals() {
        // 999.99 * 0.01 * 7 / 12 = 5.83328...
        assertEquals(5.83, peripheral(999.99, 7).warrantyCost(), EPS);
        // 100 * 0.02 * 5 / 12 = 0.8333...
        assertEquals(0.83, other(100, 5).warrantyCost(), EPS);
    }

    @Test
    void costIsNeverNegative() {
        for (Product product : List.of(laptop(0.01, 0), peripheral(0.01, 1), other(0.01, 120))) {
            assertTrue(product.warrantyCost() >= 0.0);
        }
    }

    @Test
    void mixedListIsSummedThroughTheBaseType() {
        List<Product> products = List.of(
                laptop(40000, 24), peripheral(1500, 12), other(1000, 6), laptop(5000, 0));

        double total = products.stream().mapToDouble(Product::warrantyCost).sum();

        assertEquals(2600.00 + 15.00 + 10.00 + 0.0, total, EPS);
    }

    @Test
    void fromCsvGivesSubtypesAndErrors() {
        Product first = Product.fromCsv("Ноутбук Lenovo;Ноутбуки;40000;24;3");
        Product second = Product.fromCsv("Миша Logitech;Аксесуари;1500;12;10");
        ProductValidationException badPrice =
                assertThrows(ProductValidationException.class, () -> Product.fromCsv("Ноутбук;Ноутбуки;0;12;1"));
        ProductValidationException badWarranty =
                assertThrows(ProductValidationException.class, () -> Product.fromCsv("Миша;Аксесуари;100;-1;1"));

        assertInstanceOf(Laptop.class, first);
        assertEquals(2600.00, first.warrantyCost(), EPS);
        assertInstanceOf(Peripheral.class, second);
        assertEquals(15.00, second.warrantyCost(), EPS);
        assertEquals(RecordField.PRICE, badPrice.field());
        assertEquals(ValidationError.NOT_POSITIVE, badPrice.error());
        assertEquals(RecordField.WARRANTY_MONTHS, badWarranty.field());
        assertEquals(ValidationError.NEGATIVE, badWarranty.error());
    }
}
