package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.Vendor;
import ai.leadplus.domain.vendors.VendorRepository;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorSearchServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorSearchService vendorSearchService;

    @Test
    void searchVendors_noFilters_returnsAllVendors() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Vendor> vendors = Arrays.asList(
                Vendor.builder().id(1L).companyName("A").build(),
                Vendor.builder().id(2L).companyName("B").build()
        );
        Page<Vendor> page = new PageImpl<>(vendors, pageable, vendors.size());
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vendor> result = vendorSearchService.searchVendors(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(vendorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchVendors_withSearchText_filtersByCompanyNameOrWebsite() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchText = "Acme";
        List<Vendor> vendors = Collections.singletonList(
                Vendor.builder().id(1L).companyName("Acme Corp").build()
        );
        Page<Vendor> page = new PageImpl<>(vendors, pageable, 1L);
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vendor> result = vendorSearchService.searchVendors(searchText, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCompanyName()).contains("Acme");
        verify(vendorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchVendors_withStatus_filtersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<VendorVerificationStatus> statusList = Collections.singletonList(VendorVerificationStatus.APPROVED);
        List<Vendor> vendors = Collections.singletonList(
                Vendor.builder().id(2L).companyName("B").vendorVerificationStatus(VendorVerificationStatus.APPROVED).build()
        );
        Page<Vendor> page = new PageImpl<>(vendors, pageable, 1L);
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vendor> result = vendorSearchService.searchVendors(null, statusList, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getVendorVerificationStatus()).isEqualTo(VendorVerificationStatus.APPROVED);
        verify(vendorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchVendors_withSearchTextAndStatus_filtersByBoth() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchText = "Acme";
        List<VendorVerificationStatus> statusList = Collections.singletonList(VendorVerificationStatus.PENDING);
        List<Vendor> vendors = Collections.singletonList(
                Vendor.builder().id(3L).companyName("Acme Pending").vendorVerificationStatus(VendorVerificationStatus.PENDING).build()
        );
        Page<Vendor> page = new PageImpl<>(vendors, pageable, 1L);
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vendor> result = vendorSearchService.searchVendors(searchText, statusList, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCompanyName()).contains("Acme");
        assertThat(result.getContent().getFirst().getVendorVerificationStatus()).isEqualTo(VendorVerificationStatus.PENDING);
        verify(vendorRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchVendors_emptyResult_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vendor> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vendor> result = vendorSearchService.searchVendors("Nonexistent", null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(vendorRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
