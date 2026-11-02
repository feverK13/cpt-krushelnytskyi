package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests that the factories pick the subtype by category and that the base type is enough. */
class ProductHierarchyTest {

    @Test
    void ofPicksTheSubtypeByCategory() {
        assertEquals(Laptop.class, Product.of("a", "Ноутбуки", 1, 1, 1).getClass());
        assertEquals(Peripheral.class, Product.of("a", "Монітори", 1, 1, 1).getClass());
        assertEquals(OtherProduct.class, Product.of("a", "Смартфони", 1, 1, 1).getClass());
    }

    @Test
    void fromCsvPicksTheSubtypeByCategory() {
        assertEquals(Laptop.class, Product.fromCsv("a; ноутбуки ;1;1;1").getClass());
        assertEquals(Peripheral.class, Product.fromCsv("a;Аксесуари;1;1;1").getClass());
        assertEquals(OtherProduct.class, Product.fromCsv("a;Телевізори;1;1;1").getClass());
    }

    @Test
    void builderPicksTheSubtypeByCategory() {
        assertEquals(Laptop.class, Product.builder().name("a").category("Ноутбуки").price(1).build().getClass());
        assertEquals(Peripheral.class,
                Product.builder().name("a").category("Монітори").price(1).build().getClass());
        assertEquals(OtherProduct.class,
                Product.builder().name("a").category("Побутова техніка").price(1).build().getClass());
    }

    @Test
    void kindMatchesTheCategoryOfEveryProduct() {
        assertSame(ProductKind.LAPTOP, Product.of("a", "Ноутбуки", 1, 1, 1).kind());
        assertSame(ProductKind.PERIPHERAL, Product.of("a", "Монітори", 1, 1, 1).kind());
        assertSame(ProductKind.OTHER, Product.of("a", "Смартфони", 1, 1, 1).kind());
    }

    @Test
    void subtypeConstructorRejectsForeignCategory() {
        ProductValidationException e = assertThrows(ProductValidationException.class,
                () -> new Laptop("a", "Монітори", 1, 1, 1));
        assertEquals(RecordField.CATEGORY, e.field());
        assertEquals(ValidationError.CATEGORY_MISMATCH, e.error());
        assertEquals("Монітори", e.detail());
        assertEquals("category: category does not match the product type: \"Монітори\"", e.getMessage());

        assertThrows(ProductValidationException.class, () -> new Peripheral("a", "Ноутбуки", 1, 1, 1));
        assertThrows(ProductValidationException.class, () -> new OtherProduct("a", "Ноутбуки", 1, 1, 1));
        assertThrows(ProductValidationException.class, () -> new OtherProduct("a", "Монітори", 1, 1, 1));
    }

    @Test
    void subtypeConstructorAcceptsItsOwnCategory() {
        assertEquals("Ноутбуки", new Laptop("a", " Ноутбуки ", 1, 1, 1).getCategory());
        assertEquals("Монітори", new Peripheral("a", "Монітори", 1, 1, 1).getCategory());
        assertEquals("Смартфони", new OtherProduct("a", "Смартфони", 1, 1, 1).getCategory());
    }

    @Test
    void fieldChecksComeBeforeTheKindCheck() {
        ProductValidationException e = assertThrows(ProductValidationException.class,
                () -> new Laptop("a", "Монітори", 0, 1, 1));
        assertEquals(RecordField.PRICE, e.field());
    }

    @Test
    void mixedListIsProcessedThroughTheBaseType() {
        List<Product> products = new ArrayList<>();
        products.add(Product.of("l", "Ноутбуки", 100, 24, 2));
        products.add(Product.of("p", "Монітори", 50, 12, 3));
        products.add(Product.of("o", "Смартфони", 10, 6, 5));

        Map<ProductKind, Integer> stockByKind = new EnumMap<>(ProductKind.class);
        double total = 0;
        for (Product product : products) {
            stockByKind.merge(product.kind(), product.getStock(), Integer::sum);
            total += product.getPrice();
        }

        assertEquals(160.0, total);
        assertEquals(2, stockByKind.get(ProductKind.LAPTOP));
        assertEquals(3, stockByKind.get(ProductKind.PERIPHERAL));
        assertEquals(5, stockByKind.get(ProductKind.OTHER));
        assertTrue(products.stream().allMatch(p -> p.warrantyStock().warrantyMonths() > 0));
    }
}
