package ai.leadplus.application.datapacks;

import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for data-pack access gating.
 * Applies segment and tenant visibility rules uniformly for LeadCompany and LeadContact queries.
 */
@Component
public class DataPackGate {

    /**
     * Returns predicates to AND into a query against any entity that has {@code segments}
     * and {@code tenantIds} array fields.
     * Returns empty list if no gating is active.
     * Caller is responsible for adding them to its predicate list.
     */
    public List<Predicate> buildGatePredicates(
            Path<?> entityPath,
            CriteriaBuilder cb,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess) {

        if (gatedInfo == null) {
            return Collections.emptyList();
        }

        boolean hasNamedGating = !CollectionUtils.isEmpty(gatedInfo.getNamedGatedSegments());
        boolean hasNullGating = gatedInfo.isNullGated();

        if (!hasNamedGating && !hasNullGating) {
            return Collections.emptyList();
        }

        // Fail closed: gating is active but no vendor access
        if (vendorAccess == null) {
            return List.of(cb.disjunction());
        }

        boolean hasVendorSegments = !CollectionUtils.isEmpty(vendorAccess.getNamedVendorSegments());

        if (!hasVendorSegments && !hasNullGating) {
            return Collections.emptyList();
        }

        List<Predicate> result = new ArrayList<>();

        // --- Segment gate ---
        List<Predicate> segmentArms = new ArrayList<>();

        // Arm 1: entity has named segments that overlap with vendor's segments
        if (hasVendorSegments) {
            segmentArms.add(arrayContainsAny(cb, entityPath.get("segments"), vendorAccess.getNamedVendorSegments()));
        }

        // Arm 2: entity has null/empty segments AND null gating is active AND vendor has null access
        if (hasNullGating && vendorAccess.isVendorNullAccess()) {
            segmentArms.add(cb.or(
                    cb.isNull(entityPath.get("segments")),
                    cb.equal(cb.function("cardinality", Integer.class, entityPath.get("segments")), 0)
            ));
        }

        if (segmentArms.isEmpty()) {
            // Gating is active but vendor has no segments and no null access -> nothing visible
            return List.of(cb.disjunction());
        }

        result.add(cb.or(segmentArms.toArray(new Predicate[0])));

        // --- Tenant gate ---
        if (vendorAccess.getTenantId() != null) {
            String tenantIdStr = vendorAccess.getTenantId().toString();
            result.add(cb.or(
                    cb.isNull(entityPath.get("tenantIds")),
                    cb.isTrue(cb.function("array_contains", Boolean.class,
                            entityPath.get("tenantIds"), cb.literal(tenantIdStr)))
            ));
        }

        return result;
    }

    /**
     * Convenience for callers that build a {@link Specification} directly.
     * Returns null if no gating is active.
     */
    public <T> Specification<T> buildGateSpecification(GatedInfo gatedInfo, VendorAccess vendorAccess) {
        if (gatedInfo == null) {
            return null;
        }

        boolean hasNamedGating = !CollectionUtils.isEmpty(gatedInfo.getNamedGatedSegments());
        boolean hasNullGating = gatedInfo.isNullGated();

        if (!hasNamedGating && !hasNullGating) {
            return null;
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = buildGatePredicates(root, cb, gatedInfo, vendorAccess);
            if (predicates.isEmpty()) {
                return null;
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * In-memory check, mirrors the SQL logic exactly.
     */
    public boolean isAccessible(
            List<String> segments,
            List<String> tenantIds,
            GatedInfo gatedInfo,
            VendorAccess vendorAccess) {

        // --- Tenant gate ---
        if (!CollectionUtils.isEmpty(tenantIds)) {
            if (vendorAccess == null || vendorAccess.getTenantId() == null) {
                return false;
            }
            String vendorTenant = vendorAccess.getTenantId().toString();
            if (!tenantIds.contains(vendorTenant)) {
                return false;
            }
        }

        // --- Segment gate ---
        if (gatedInfo == null) {
            return true;
        }

        boolean hasNamedGating = !CollectionUtils.isEmpty(gatedInfo.getNamedGatedSegments());
        boolean hasNullGating = gatedInfo.isNullGated();

        if (!hasNamedGating && !hasNullGating) {
            return true;
        }

        // Fail closed
        if (vendorAccess == null) {
            return false;
        }

        boolean hasVendorSegments = !CollectionUtils.isEmpty(vendorAccess.getNamedVendorSegments());

        // Entity has null/empty segments
        if (CollectionUtils.isEmpty(segments)) {
            if (!hasNullGating) {
                return true; // not gated at all for null segments
            }
            return vendorAccess.isVendorNullAccess();
        }

        // Entity has named segments — check overlap with vendor's segments
        if (hasVendorSegments) {
            boolean overlaps = segments.stream()
                    .anyMatch(vendorAccess.getNamedVendorSegments()::contains);
            if (overlaps) {
                return true;
            }
        }

        // Entity has named segments but none overlap with vendor's — check if any are gated
        if (hasNamedGating) {
            boolean hasGatedSegment = segments.stream()
                    .anyMatch(gatedInfo.getNamedGatedSegments()::contains);
            if (hasGatedSegment) {
                return false; // entity has gated segments vendor can't access
            }
        }

        // Entity has segments but none of them are gated -> accessible
        return true;
    }

    private Predicate arrayContainsAny(
            CriteriaBuilder cb,
            Path<List<String>> arrayPath,
            List<String> values) {

        List<Predicate> preds = new ArrayList<>();
        for (String v : values) {
            preds.add(cb.isTrue(
                    cb.function("array_contains", Boolean.class, arrayPath, cb.literal(v))
            ));
        }
        return cb.or(preds.toArray(new Predicate[0]));
    }
}
