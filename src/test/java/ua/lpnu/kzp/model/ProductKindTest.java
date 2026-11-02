package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link ProductKind}: category mapping, fallback and subtype creation. */
class ProductKindTest {

    @ParameterizedTest
    @ValueSource(strings = {"Ноутбуки", "ноутбук", "  НОУТБУКИ\t", "Laptops", "notebook"})
    void mapsLaptopCategories(String category) {
        assertTrue(ProductKind.fromCategory(category) == ProductKind.LAPTOP);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Аксесуари", "Монітори", " монітор ", "Навушники", "Аудіо", "Keyboards", "MICE"})
    void mapsPeripheralCategories(String category) {
        assertTrue(ProductKind.fromCategory(category) == ProductKind.PERIPHERAL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "Побутова техніка", "Смартфони", "Телевізори", "unknown"})
    void mapsBlankNullAndUnknownToOther(String category) {
        assertTrue(ProductKind.fromCategory(category) == ProductKind.OTHER);
    }

    @Test
    void acceptsOnlyItsOwnCategories() {
        assertTrue(ProductKind.LAPTOP.accepts("Ноутбуки"));
        assertFalse(ProductKind.LAPTOP.accepts("Монітори"));
        assertTrue(ProductKind.PERIPHERAL.accepts(" Монітори "));
        assertFalse(ProductKind.PERIPHERAL.accepts(null));
        assertTrue(ProductKind.OTHER.accepts(null));
        assertTrue(ProductKind.OTHER.accepts("Смартфони"));
        assertFalse(ProductKind.OTHER.accepts("Ноутбуки"));
    }

    @Test
    void categoriesAreLowerCaseAndDisjoint() {
        for (ProductKind kind : ProductKind.values()) {
            for (String category : kind.categories()) {
                assertEquals(category.toLowerCase(java.util.Locale.ROOT), category);
                assertTrue(ProductKind.fromCategory(category) == kind, category);
            }
        }
        assertTrue(ProductKind.OTHER.categories().isEmpty());
    }

    @Test
    void labelsAreNotBlank() {
        for (ProductKind kind : ProductKind.values()) {
            assertFalse(kind.label().isBlank());
        }
    }

    @Test
    void createBuildsTheSubtypeOfTheKind() {
        assertTrue(ProductKind.LAPTOP.create("a", "Ноутбуки", 1, 1, 1) instanceof Laptop);
        assertTrue(ProductKind.PERIPHERAL.create("a", "Монітори", 1, 1, 1) instanceof Peripheral);
        assertTrue(ProductKind.OTHER.create("a", "Смартфони", 1, 1, 1) instanceof OtherProduct);
    }
}
