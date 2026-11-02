package ua.lpnu.kzp.metrics;

import ua.lpnu.kzp.model.Product;
import ua.lpnu.kzp.model.ProductKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Warranty cost of the valid products grouped by {@link ProductKind}.
 *
 * <p>Costs are summed through the base type {@link Product#warrantyCost()}; groups follow the
 * order of {@link ProductKind} and kinds without products are omitted.</p>
 *
 * @param groups groups in {@link ProductKind} order, never {@code null}
 * @param total  warranty cost of all products, in UAH
 */
public record WarrantyCostSummary(List<Group> groups, double total) {

    /**
     * Products of one kind.
     *
     * @param kind           the kind
     * @param count          number of products of this kind, at least 1
     * @param subtotal       sum of their warranty costs, in UAH
     * @param representative the first product of this kind in input order
     */
    public record Group(ProductKind kind, int count, double subtotal, Product representative) {
    }

    /** Copies the group list so the record stays immutable. */
    public WarrantyCostSummary {
        groups = List.copyOf(groups);
    }

    /** Returns the groups; the list is unmodifiable. */
    @Override
    public List<Group> groups() {
        return List.copyOf(groups);
    }

    /**
     * Groups the products by kind and sums their warranty costs.
     *
     * @param products valid products, may be empty
     * @return the summary; with no products it has no groups and a total of 0
     */
    public static WarrantyCostSummary of(List<Product> products) {
        Map<ProductKind, List<Product>> byKind = new EnumMap<>(ProductKind.class);
        for (Product product : products) {
            byKind.computeIfAbsent(product.kind(), kind -> new ArrayList<>()).add(product);
        }
        List<Group> groups = new ArrayList<>();
        double total = 0;
        for (ProductKind kind : ProductKind.values()) {
            List<Product> members = byKind.get(kind);
            if (members == null) {
                continue;
            }
            double subtotal = 0;
            for (Product member : members) {
                subtotal += member.warrantyCost();
            }
            groups.add(new Group(kind, members.size(), subtotal, members.get(0)));
            total += subtotal;
        }
        return new WarrantyCostSummary(groups, total);
    }

    /**
     * Renders the summary as a single English line for the log.
     *
     * @return e.g. {@code groups=3 total=4906.82}
     */
    public String summary() {
        return String.format(Locale.ROOT, "groups=%d total=%.2f", groups.size(), total);
    }
}
