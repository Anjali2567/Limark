package ai.leadplus.application.vendors;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.exception.UnauthorizedException;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.common.TestSecurityUtils;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.vendordatapacks.VendorDataPackRepository;
import ai.leadplus.domain.vendors.Vendor;
import ai.leadplus.domain.vendors.VendorRepository;
import ai.leadplus.domain.vendors.VendorVerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorSearchService vendorSearchService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LeadDataPackService leadDataPackService;

    @Mock
    private VendorDataPackRepository vendorDataPackRepository;

    @InjectMocks
    private VendorService vendorService;

    private Vendor vendor;
    private VendorDto vendorDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        vendor = Vendor.builder()
                .id(1L)
                .userId(1L)
                .tenantId(1L)
                .companyName("Company A")
                .description("Desc")
                .vendorVerificationStatus(VendorVerificationStatus.PENDING)
                .active(true)
                .build();

        vendorDto = VendorDto.builder()
                .userId(1L)
                .tenantId(1L)
                .companyName("Company A")
                .description("Desc")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .tenantId(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        ReflectionTestUtils.setField(vendorService, "clientUrl", "http://localhost:8080");

        TestSecurityUtils.clear();
    }

    @Test
    void createVendor_shouldSetDefaultStatus_whenNull() {
        vendorDto.setVendorVerificationStatus(null);

        when(vendorRepository.save(any()))
                .thenAnswer(invocation -> {
                    Vendor v = invocation.getArgument(0);
                    v.setId(1L);
                    return v;
                });

        VendorDto result = vendorService.createVendor(vendorDto);

        assertThat(result.getVendorVerificationStatus())
                .isEqualTo(VendorVerificationStatus.INCOMPLETE);
    }

    @Test
    void createVendor_shouldKeepProvidedStatus() {
        vendorDto.setVendorVerificationStatus(VendorVerificationStatus.APPROVED);

        when(vendorRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        VendorDto result = vendorService.createVendor(vendorDto);

        assertThat(result.getVendorVerificationStatus())
                .isEqualTo(VendorVerificationStatus.APPROVED);
    }

    @Test
    void createVendorFromUser_shouldCreateVendor() {
        when(vendorRepository.save(any())).thenReturn(vendor);
        when(leadDataPackService.findBySlug("software")).thenReturn(Optional.empty());

        vendorService.createVendorFromUser(userDto);

        verify(vendorRepository).save(any());
    }

    @Test
    void getAuthenticatedVendor_shouldReturnVendor() {
        TestSecurityUtils.mockAuthenticatedUser(String.valueOf(1L));

        when(vendorRepository.findByUserIdAndActiveTrue(1L))
                .thenReturn(Optional.of(vendor));

        VendorDto result = vendorService.getAuthenticatedVendor();

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void validateAuthenticatedVendorId_shouldReturnVendor_whenMatch() {
        TestSecurityUtils.mockAuthenticatedUser("1");

        when(vendorRepository.findByUserIdAndActiveTrue(1L))
                .thenReturn(Optional.of(vendor));

        VendorDto result = vendorService.validateAuthenticatedVendorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void validateAuthenticatedVendorId_shouldThrow_whenMismatch() {
        TestSecurityUtils.mockAuthenticatedUser("1");

        when(vendorRepository.findByUserIdAndActiveTrue(1L))
                .thenReturn(Optional.of(vendor));

        assertThatThrownBy(() ->
                vendorService.validateAuthenticatedVendorId(2L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getAllVendors_shouldReturnPageOfVendors() {
        Vendor vendor1 = Vendor.builder().id(1L).companyName("Company A").build();
        Vendor vendor2 = Vendor.builder().id(2L).companyName("Company B").build();

        Page<Vendor> vendorPage = new PageImpl<>(
                List.of(vendor1, vendor2),
                PageRequest.of(0, 10),
                2
        );

        when(vendorSearchService.searchVendors(null, null, PageRequest.of(0, 10)))
                .thenReturn(vendorPage);

        Page<VendorDto> result =
                vendorService.getAllVendors(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void updateVendorStatus_shouldUpdateStatus() {
        when(vendorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(vendor));
        when(vendorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userService.getUserById(1L)).thenReturn(userDto);

        VendorDto result = vendorService.updateVendorStatus(1L, VendorVerificationStatus.APPROVED);

        assertThat(result.getVendorVerificationStatus()).isEqualTo(VendorVerificationStatus.APPROVED);

        verify(emailService).sendVendorApprovedEmail(
                eq(userDto.getEmail()),
                eq(userDto.getName()),
                eq("http://localhost:8080/login") // matches the injected clientUrl
        );
    }

    @Test
    void updateVendorStatus_withComment_shouldUpdateStatusAndSendEmail() {
        when(vendorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(vendor));
        when(vendorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userService.getUserById(1L)).thenReturn(userDto);

        String reviewComment = "Does not meet criteria";
        VendorDto result = vendorService.updateVendorStatus(1L, VendorVerificationStatus.REJECTED, reviewComment);

        assertThat(result.getVendorVerificationStatus()).isEqualTo(VendorVerificationStatus.REJECTED);
        assertThat(result.getReviewComment()).isEqualTo(reviewComment);
        verify(emailService).sendVendorRejectedEmail(userDto.getEmail(), userDto.getName(), reviewComment);
    }

    @Test
    void updateVendorStatus_shouldThrow_whenVendorNotFound() {
        when(vendorRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                vendorService.updateVendorStatus(999L, VendorVerificationStatus.APPROVED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found with id: 999");

        verify(vendorRepository).findByIdAndActiveTrue(999L);
        verify(vendorRepository, never()).save(any(Vendor.class));
    }

    @Test
    void updateVendorStatus_withComment_shouldThrow_whenVendorNotFound() {
        when(vendorRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                vendorService.updateVendorStatus(999L, VendorVerificationStatus.APPROVED, "Comment"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found with id: 999");

        verify(vendorRepository).findByIdAndActiveTrue(999L);
        verify(vendorRepository, never()).save(any(Vendor.class));
    }
}
