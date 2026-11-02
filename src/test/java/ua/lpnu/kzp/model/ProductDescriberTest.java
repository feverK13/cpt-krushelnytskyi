package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of {@link ProductDescriber} and of the sealed hierarchy. */
class ProductDescriberTest {

    @Test
    void describesLaptop() {
        assertEquals("3% ціни за кожен рік гарантії + 100 грн за кожен розпочатий рік",
                ProductDescriber.describe(Product.of("a", "Ноутбуки", 1, 1, 1)));
    }

    @Test
    void describesPeripheral() {
        assertEquals("1% ціни за кожен рік гарантії",
                ProductDescriber.describe(Product.of("a", "Монітори", 1, 1, 1)));
    }

    @Test
    void describesOtherProduct() {
        assertEquals("2% ціни за кожен рік гарантії",
                ProductDescriber.describe(Product.of("a", "Смартфони", 1, 1, 1)));
    }

    @Test
    void productIsSealedWithTheThreeSubtypes() {
        assertTrue(Product.class.isSealed());
        assertEquals(
                Set.of(Laptop.class, Peripheral.class, OtherProduct.class),
                Set.of(Product.class.getPermittedSubclasses()));
    }

    @Test
    void rateConstantsMatchTheDocumentedPolicy() {
        assertEquals(0.03, Laptop.RATE_PER_YEAR);
        assertEquals(100.0, Laptop.SURCHARGE_PER_STARTED_YEAR);
        assertEquals(0.01, Peripheral.RATE_PER_YEAR);
        assertEquals(0.02, OtherProduct.RATE_PER_YEAR);
    }
}
