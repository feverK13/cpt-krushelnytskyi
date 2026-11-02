package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of {@code equals} and {@code hashCode}: same class and all five fields. */
class ProductEqualityTest {

    private static Product laptop() {
        return Product.of("Ноутбук", "Ноутбуки", 100.5, 24, 7);
    }

    @Test
    void isReflexive() {
        Product product = laptop();

        assertEquals(product, product);
    }

    @Test
    void isSymmetric() {
        Product a = laptop();
        Product b = laptop();

        assertTrue(a.equals(b) && b.equals(a));
    }

    @Test
    void isTransitive() {
        Product a = laptop();
        Product b = laptop();
        Product c = laptop();

        assertTrue(a.equals(b) && b.equals(c) && a.equals(c));
    }

    @Test
    void isNotEqualToNullOrAnotherClass() {
        Product product = laptop();

        assertFalse(product.equals(null));
        assertNotEquals("Ноутбук", product);
        assertNotEquals(new Object(), product);
    }

    @Test
    void eachFieldMatters() {
        Product base = laptop();

        assertNotEquals(base, Product.of("Інший", "Ноутбуки", 100.5, 24, 7));
        assertNotEquals(base, Product.of("Ноутбук", "Laptops", 100.5, 24, 7));
        assertNotEquals(base, Product.of("Ноутбук", "Ноутбуки", 100.51, 24, 7));
        assertNotEquals(base, Product.of("Ноутбук", "Ноутбуки", 100.5, 25, 7));
        assertNotEquals(base, Product.of("Ноутбук", "Ноутбуки", 100.5, 24, 8));
    }

    @Test
    void equalObjectsHaveEqualStableHashCodes() {
        Product a = laptop();
        Product b = laptop();

        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    void factoriesWithTheSameDataGiveEqualProducts() {
        Product viaOf = Product.of("Ноутбук", "Ноутбуки", 100.5, 24, 7);
        Product viaCsv = Product.fromCsv(" Ноутбук ; Ноутбуки ;100.50;24;7");
        Product viaBuilder = Product.builder()
                .name("Ноутбук").category("Ноутбуки").price(100.5).warrantyMonths(24).stock(7).build();

        assertEquals(viaOf, viaCsv);
        assertEquals(viaOf, viaBuilder);
        assertEquals(viaOf.hashCode(), viaCsv.hashCode());
        assertEquals(viaOf.hashCode(), viaBuilder.hashCode());
    }

    @Test
    void differentSubtypesAreNeverEqual() {
        Product laptop = new Laptop("Ноутбук", "Ноутбуки", 1, 1, 1);
        Product peripheral = new Peripheral("Ноутбук", "Монітори", 1, 1, 1);
        Product other = new OtherProduct("Ноутбук", "Смартфони", 1, 1, 1);

        assertNotEquals(laptop, peripheral);
        assertNotEquals(peripheral, laptop);
        assertNotEquals(laptop, other);
        assertNotEquals(peripheral, other);
    }

    @Test
    void hashSetCollapsesDuplicatesOfMixedSubtypes() {
        Set<Product> set = new HashSet<>(List.of(
                Product.of("a", "Ноутбуки", 1, 1, 1),
                Product.of("b", "Монітори", 2, 2, 2),
                Product.of("c", "Смартфони", 3, 3, 3)));

        assertFalse(set.add(Product.of("a", "Ноутбуки", 1, 1, 1)));
        assertFalse(set.add(Product.fromCsv("b;Монітори;2;2;2")));
        assertTrue(set.add(Product.of("a", "Ноутбуки", 1, 1, 2)));

        assertEquals(4, set.size());
        assertTrue(set.contains(Product.of("c", "Смартфони", 3, 3, 3)));
        assertFalse(set.contains(Product.of("c", "Смартфони", 3, 3, 4)));
    }

    @Test
    void hashMapFindsValuesByEqualKeys() {
        Map<Product, String> map = new HashMap<>();
        map.put(Product.of("a", "Ноутбуки", 1, 1, 1), "laptop");
        map.put(Product.of("b", "Монітори", 2, 2, 2), "peripheral");
        map.put(Product.of("c", "Смартфони", 3, 3, 3), "other");

        assertEquals("laptop", map.get(Product.fromCsv("a;Ноутбуки;1;1;1")));
        assertEquals("peripheral", map.get(Product.of("b", "Монітори", 2, 2, 2)));
        assertEquals("other", map.get(Product.of("c", "Смартфони", 3, 3, 3)));
        assertNull(map.get(Product.of("a", "Ноутбуки", 1, 1, 2)));
        assertEquals(3, map.size());
    }
}
