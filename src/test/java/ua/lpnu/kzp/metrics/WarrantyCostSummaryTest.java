package ua.lpnu.kzp.metrics;

import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.model.Laptop;
import ua.lpnu.kzp.model.OtherProduct;
import ua.lpnu.kzp.model.Peripheral;
import ua.lpnu.kzp.model.Product;
import ua.lpnu.kzp.model.ProductKind;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of {@link WarrantyCostSummary} with hand-calculated numbers. */
class WarrantyCostSummaryTest {

    private static final double EPS = 1e-9;

    @Test
    void groupsInKindOrderWithCountsAndSubtotals() {
        Product other1 = Product.of("o1", "Телевізори", 1000, 6, 1);     // 10.00
        Product laptop1 = Product.of("l1", "Ноутбуки", 40000, 24, 1);    // 2600.00
        Product peripheral = Product.of("p1", "Монітори", 1500, 12, 1);  // 15.00
        Product other2 = Product.of("o2", "Смартфони", 2000, 12, 1);     // 40.00
        Product laptop2 = Product.of("l2", "Ноутбуки", 10000, 18, 1);    // 650.00

        WarrantyCostSummary summary =
                WarrantyCostSummary.of(List.of(other1, laptop1, peripheral, other2, laptop2));

        assertEquals(3, summary.groups().size());
        WarrantyCostSummary.Group laptops = summary.groups().get(0);
        assertSame(ProductKind.LAPTOP, laptops.kind());
        assertEquals(2, laptops.count());
        assertEquals(3250.00, laptops.subtotal(), EPS);
        assertSame(laptop1, laptops.representative());
        assertInstanceOf(Laptop.class, laptops.representative());
        WarrantyCostSummary.Group peripherals = summary.groups().get(1);
        assertSame(ProductKind.PERIPHERAL, peripherals.kind());
        assertEquals(15.00, peripherals.subtotal(), EPS);
        assertInstanceOf(Peripheral.class, peripherals.representative());
        WarrantyCostSummary.Group others = summary.groups().get(2);
        assertSame(ProductKind.OTHER, others.kind());
        assertEquals(2, others.count());
        assertEquals(50.00, others.subtotal(), EPS);
        assertSame(other1, others.representative());
        assertInstanceOf(OtherProduct.class, others.representative());
        assertEquals(3315.00, summary.total(), EPS);
        assertEquals("groups=3 total=3315.00", summary.summary());
    }

    @Test
    void kindsWithoutProductsAreOmitted() {
        WarrantyCostSummary summary =
                WarrantyCostSummary.of(List.of(Product.of("p1", "Монітори", 1500, 12, 1)));

        assertEquals(1, summary.groups().size());
        assertSame(ProductKind.PERIPHERAL, summary.groups().get(0).kind());
    }

    @Test
    void emptyInputGivesNoGroupsAndZeroTotal() {
        WarrantyCostSummary summary = WarrantyCostSummary.of(List.of());

        assertTrue(summary.groups().isEmpty());
        assertEquals(0.0, summary.total(), EPS);
    }

    @Test
    void groupListIsUnmodifiable() {
        WarrantyCostSummary summary = WarrantyCostSummary.of(List.of());

        assertThrows(UnsupportedOperationException.class, () -> summary.groups().add(null));
    }
}
