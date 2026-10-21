package ua.lpnu.kzp.metrics;

import ua.lpnu.kzp.model.Product;
import ua.lpnu.kzp.model.WarrantyStock;

import java.util.List;
import java.util.Optional;

/**
 * Computes the report metrics over valid products.
 */
public final class MetricsCalculator {

    private MetricsCalculator() {
    }

    /**
     * Computes the count, average price, longest warranty and total stock.
     *
     * <p>The stock is summed as {@code long}, so values near {@link Integer#MAX_VALUE} do not
     * overflow.</p>
     *
     * @param products valid products
     * @return the metrics, or an empty optional if there are no products
     */
    public static Optional<Metrics> calculate(List<Product> products) {
        if (products.isEmpty()) {
            return Optional.empty();
        }

        double priceSum = 0;
        int longestWarranty = Integer.MIN_VALUE;
        long totalStock = 0;
        for (Product product : products) {
            WarrantyStock warrantyStock = product.warrantyStock();
            priceSum += product.getPrice();
            longestWarranty = Math.max(longestWarranty, warrantyStock.warrantyMonths());
            totalStock += warrantyStock.stock();
        }

        int count = products.size();
        return Optional.of(new Metrics(count, priceSum / count, longestWarranty, totalStock));
    }
}
