package ai.leadplus.application.tenantleadfilters;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.common.LeadType;
import ai.leadplus.domain.tenantleadfilters.TenantLeadFilter;
import ai.leadplus.domain.tenantleadfilters.TenantLeadFilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantLeadFilterServiceTest {

    @Mock
    private TenantLeadFilterRepository repository;

    @InjectMocks
    private TenantLeadFilterService service;

    private TenantLeadFilter entity;
    private TenantLeadFilterDto dto;

    @BeforeEach
    void setup() {
        entity = TenantLeadFilter.builder()
                .id(1L)
                .tenantId(1L)
                .type(LeadType.LEAD_CONTACT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        dto = TenantLeadFilterDto.fromEntity(entity);
    }

    @Test
    void createTenantLeadFilter_shouldSaveAndReturnDto() {
        when(repository.save(any(TenantLeadFilter.class))).thenReturn(entity);

        TenantLeadFilterDto result = service.createTenantLeadFilter(dto);

        assertNotNull(result);
        assertTrue(result.isActive());
        verify(repository, times(1)).save(any(TenantLeadFilter.class));
    }

    @Test
    void getTenantLeadFilterById_shouldReturnDto_whenExists() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(entity));

        TenantLeadFilterDto result = service.getTenantLeadFilterById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getTenantLeadFilterById_shouldThrowException_whenNotFound() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTenantLeadFilterById(1L, 1L));
    }

    @Test
    void getTenantLeadFilterByTenantId_shouldReturnList() {
        when(repository.findByTenantIdAndActiveTrue(1L))
                .thenReturn(List.of(entity));

        List<TenantLeadFilterDto> result = service.getTenantLeadFilterByTenantId(1L);

        assertEquals(1, result.size());
        verify(repository).findByTenantIdAndActiveTrue(1L);
    }

    @Test
    void updateTenantLeadFilter_shouldUpdateSuccessfully() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(entity));

        when(repository.save(any(TenantLeadFilter.class)))
                .thenReturn(entity);

        dto.setTenantId(1L);

        TenantLeadFilterDto result = service.updateTenantLeadFilter(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(any(TenantLeadFilter.class));
    }

    @Test
    void updateTenantLeadFilter_shouldThrowException_whenNotFound() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());

        dto.setTenantId(1L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTenantLeadFilter(1L, dto));
    }

    @Test
    void deleteTenantLeadFilter_shouldSoftDelete() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(entity));

        when(repository.save(any(TenantLeadFilter.class)))
                .thenReturn(entity);

        service.deleteTenantLeadFilter(1L, 1L);

        verify(repository).save(any(TenantLeadFilter.class));
    }

    @Test
    void deleteTenantLeadFilter_shouldThrowException_whenNotFound() {
        when(repository.findByIdAndTenantIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteTenantLeadFilter(1L, 1L));
    }
}
