package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.Vendor;
import ai.leadplus.domain.vendors.VendorRepository;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorSearchService {

    private final VendorRepository vendorRepository;

    public Page<Vendor> searchVendors(String searchText, List<VendorVerificationStatus> vendorVerificationStatus, Pageable pageable) {
        return searchVendors(searchText, null, null, null, null, vendorVerificationStatus, pageable);
    }

    public Page<Vendor> searchVendors(
            List<Long> industryIds,
            List<Long> serviceIds,
            List<Long> specificationIds,
            List<String> certifications,
            List<VendorVerificationStatus> vendorVerificationStatus,
            Pageable pageable
    ) {
        return searchVendors(null, industryIds, serviceIds, specificationIds, certifications, vendorVerificationStatus, pageable);
    }

    private Page<Vendor> searchVendors(
            String searchText,
            List<Long> industryIds,
            List<Long> serviceIds,
            List<Long> specificationIds,
            List<String> certifications,
            List<VendorVerificationStatus> vendorVerificationStatus,
            Pageable pageable
    ) {
        Specification<Vendor> spec = Specification.where(null);

        if (StringUtils.hasText(searchText)) {
            String[] searchWords = searchText.trim().split("\\s+");
            for (String word : searchWords) {
                String searchPattern = "%" + word.toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("companyName")), searchPattern),
                                cb.like(cb.lower(root.get("website")), searchPattern)
                        )
                );
            }
        }

        if (!CollectionUtils.isEmpty(industryIds)) {
            spec = spec.and((root, query, cb) -> root.join("industryIds").in(industryIds));
        }

        if (!CollectionUtils.isEmpty(serviceIds)) {
            spec = spec.and((root, query, cb) -> root.join("serviceIds").in(serviceIds));
        }

        if (!CollectionUtils.isEmpty(specificationIds)) {
            spec = spec.and((root, query, cb) -> root.join("specificationIds").in(specificationIds));
        }

        if (!CollectionUtils.isEmpty(certifications)) {
            spec = spec.and((root, query, cb) -> root.join("certifications").in(certifications));
        }

        if (!CollectionUtils.isEmpty(vendorVerificationStatus)) {
            spec = spec.and((root, query, cb) -> root.get("vendorVerificationStatus").in(vendorVerificationStatus));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        return vendorRepository.findAll(spec, pageable);
    }
}
