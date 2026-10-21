package ua.lpnu.kzp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Product.Builder}: a valid build, independence of built products and rejection
 * of every invalid value exactly like the {@link Product} constructor.
 */
class ProductBuilderTest {

    private static final String NAME = "Ноутбук Lenovo IdeaPad 3";
    private static final String CATEGORY = "Ноутбуки";

    private static Product.Builder valid() {
        return Product.builder()
                .name(NAME)
                .category(CATEGORY)
                .price(24999.00)
                .warrantyMonths(24)
                .stock(7);
    }

    private static void assertSameRejection(Executable direct, Executable viaBuilder) {
        Throwable expected = assertThrows(Throwable.class, direct);
        Throwable actual = assertThrows(Throwable.class, viaBuilder);

        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getMessage(), actual.getMessage());
        if (expected instanceof ProductValidationException e) {
            ProductValidationException a = (ProductValidationException) actual;
            assertEquals(e.field(), a.field());
            assertEquals(e.error(), a.error());
            assertEquals(e.detail(), a.detail());
        }
    }

    @Test
    void buildsAValidProduct() {
        Product product = valid().build();

        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(24999.00, product.getPrice());
        assertEquals(24, product.getWarrantyMonths());
        assertEquals(7, product.getStock());
    }

    @Test
    void settersReturnTheSameBuilder() {
        Product.Builder builder = Product.builder();

        assertSame(builder, builder.name(NAME));
        assertSame(builder, builder.category(CATEGORY));
        assertSame(builder, builder.price(1));
        assertSame(builder, builder.warrantyMonths(1));
        assertSame(builder, builder.stock(1));
    }

    @Test
    void trimsNameAndCategoryLikeTheConstructor() {
        Product product = valid().name("  " + NAME + "\t").category(" \t" + CATEGORY + "  ").build();

        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
    }

    @Test
    void everyBuildReturnsANewProduct() {
        Product.Builder builder = valid();

        Product first = builder.build();
        Product second = builder.build();

        assertNotSame(first, second);
        assertEquals(first.toString(), second.toString());
    }

    @Test
    void changingTheBuilderAfterBuildDoesNotAffectTheProduct() {
        Product.Builder builder = valid();
        Product product = builder.build();

        builder.name("Інша назва").category("Інша").price(1).warrantyMonths(1).stock(1);

        assertEquals(NAME, product.getName());
        assertEquals(CATEGORY, product.getCategory());
        assertEquals(24999.00, product.getPrice());
        assertEquals(24, product.getWarrantyMonths());
        assertEquals(7, product.getStock());
    }

    @Test
    void missingNameFailsLikeANullNameInTheConstructor() {
        assertSameRejection(
                () -> new Product(null, CATEGORY, 1, 1, 1),
                () -> Product.builder().category(CATEGORY).price(1).warrantyMonths(1).stock(1).build());
    }

    @Test
    void missingCategoryFailsLikeANullCategoryInTheConstructor() {
        assertSameRejection(
                () -> new Product(NAME, null, 1, 1, 1),
                () -> Product.builder().name(NAME).price(1).warrantyMonths(1).stock(1).build());
    }

    @Test
    void missingPriceFailsLikeTheDefaultPriceInTheConstructor() {
        assertSameRejection(
                () -> new Product(NAME, CATEGORY, 0.0, 1, 1),
                () -> Product.builder().name(NAME).category(CATEGORY).warrantyMonths(1).stock(1).build());
    }

    @Test
    void missingWarrantyAndStockDefaultToZeroLikeTheConstructor() {
        Product product = Product.builder().name(NAME).category(CATEGORY).price(1).build();

        assertEquals(0, product.getWarrantyMonths());
        assertEquals(0, product.getStock());
    }

    @Test
    void nullNameAndCategoryFailLikeTheConstructor() {
        assertSameRejection(
                () -> new Product(null, CATEGORY, 1, 1, 1), () -> valid().name(null).build());
        assertSameRejection(
                () -> new Product(NAME, null, 1, 1, 1), () -> valid().category(null).build());
        assertThrows(NullPointerException.class, () -> valid().name(null).build());
        assertThrows(NullPointerException.class, () -> valid().category(null).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t "})
    void blankNameFailsLikeTheConstructor(String name) {
        assertSameRejection(
                () -> new Product(name, CATEGORY, 1, 1, 1), () -> valid().name(name).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", " \t "})
    void blankCategoryFailsLikeTheConstructor(String category) {
        assertSameRejection(
                () -> new Product(NAME, category, 1, 1, 1), () -> valid().category(category).build());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
        0.0, -0.0, -0.01, -1, -24999.00,
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
    })
    void invalidPriceFailsLikeTheConstructor(double price) {
        assertSameRejection(
                () -> new Product(NAME, CATEGORY, price, 1, 1), () -> valid().price(price).build());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 1, 24999.00, Double.MAX_VALUE})
    void validPriceIsAcceptedLikeTheConstructor(double price) {
        assertEquals(price, valid().price(price).build().getPrice());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -12, Integer.MIN_VALUE})
    void negativeWarrantyFailsLikeTheConstructor(int warranty) {
        assertSameRejection(
                () -> new Product(NAME, CATEGORY, 1, warranty, 1),
                () -> valid().warrantyMonths(warranty).build());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void validWarrantyIsAcceptedLikeTheConstructor(int warranty) {
        assertEquals(warranty, valid().warrantyMonths(warranty).build().getWarrantyMonths());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -3, Integer.MIN_VALUE})
    void negativeStockFailsLikeTheConstructor(int stock) {
        assertSameRejection(
                () -> new Product(NAME, CATEGORY, 1, 1, stock), () -> valid().stock(stock).build());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void validStockIsAcceptedLikeTheConstructor(int stock) {
        assertEquals(stock, valid().stock(stock).build().getStock());
    }

    @Test
    void firstInvalidFieldWinsLikeInTheConstructor() {
        assertSameRejection(
                () -> new Product(" ", CATEGORY, 0, -1, -1),
                () -> valid().name(" ").price(0).warrantyMonths(-1).stock(-1).build());
        assertSameRejection(
                () -> new Product(NAME, CATEGORY, 0, -1, -1),
                () -> valid().price(0).warrantyMonths(-1).stock(-1).build());
    }
}
