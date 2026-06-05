package ai.leadplus.application.vendordatapacks;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.vendordatapacks.VendorDataPack;
import ai.leadplus.domain.vendordatapacks.VendorDataPackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorDataPackServiceTest {

    @Mock
    private VendorDataPackRepository repository;

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorDataPackService service;

    private VendorDataPack entity;
    private VendorDataPackDto dto;
    private VendorDto vendorDto;

    private final Long tenantId = 1L;
    private final Long vendorId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setup() {
        entity = VendorDataPack.builder()
                .id(1L)
                .tenantId(tenantId)
                .vendorId(vendorId)
                .leadDataPackId(1L)
                .assignedBy(String.valueOf(userId))
                .active(true)
                .build();

        dto = VendorDataPackDto.builder()
                .id(1L)
                .vendorId(vendorId)
                .leadDataPackId(1L)
                .build();

        vendorDto = VendorDto.builder()
                .id(1L)
                .tenantId(tenantId)
                .build();
    }

    @Test
    void createVendorDataPack_shouldSetTenantAssignedByAndActive() {
        when(vendorService.getVendorById(vendorId)).thenReturn(vendorDto);
        when(repository.save(any())).thenReturn(entity);

        VendorDataPackDto result = service.createVendorDataPack(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAssignedBy()).isEqualTo(String.valueOf(userId));

        verify(vendorService).getVendorById(vendorId);
        verify(repository).save(any(VendorDataPack.class));
    }

    @Test
    void getById_shouldReturnDto_whenExists() {
        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));

        VendorDataPackDto result = service.getById("1");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVendorId()).isEqualTo(vendorId);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("VendorDataPack not found with id: 1");
    }

    @Test
    void getAll_shouldReturnList() {
        when(repository.findAllByTenantIdAndVendorIdAndActiveTrue(tenantId, vendorId))
                .thenReturn(List.of(entity));

        List<VendorDataPackDto> result = service.getAll(String.valueOf(tenantId), String.valueOf(vendorId));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getVendorId()).isEqualTo(vendorId);
    }

    @Test
    void deleteVendorDataPackById_shouldSoftDelete() {
        when(vendorService.getVendorById(vendorId)).thenReturn(vendorDto);
        when(repository.findByIdAndTenantIdAndVendorIdAndActiveTrue(1L, tenantId, vendorId))
                .thenReturn(Optional.of(entity));

        service.deleteVendorDataPackById(1L, vendorId);

        assertThat(entity.isActive()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void deleteVendorDataPackById_shouldThrow_whenNotFound() {
        when(vendorService.getVendorById(vendorId)).thenReturn(vendorDto);
        when(repository.findByIdAndTenantIdAndVendorIdAndActiveTrue(1L, tenantId, vendorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteVendorDataPackById(1L, vendorId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
