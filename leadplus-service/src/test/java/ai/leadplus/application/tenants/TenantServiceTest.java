package ai.leadplus.application.tenants;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.tenants.Tenant;
import ai.leadplus.domain.tenants.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createTenant_shouldSaveAndReturnTenantDto() {
        LocalDateTime now = LocalDateTime.now();

        String tenantName = "Test Tenant";

        Tenant savedEntity = Tenant.builder()
                .id(1L)
                .name("Test Tenant")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedEntity);

        TenantDto result = tenantService.createTenant(tenantName);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Tenant");
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void getTenant_shouldReturnTenantDto_WhenFound() {
        Tenant entity = Tenant.builder()
                .id(1L)
                .name("My Tenant")
                .ownerId(9L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(entity));

        TenantDto result = tenantService.getTenant("1");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("My Tenant");
        assertThat(result.getOwnerId()).isEqualTo(9L);

        verify(tenantRepository).findById(1L);
    }

    @Test
    void getTenant_shouldThrowException_WhenNotFound() {
        when(tenantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.getTenant("2"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tenant not found with id: 2.");

        verify(tenantRepository).findById(2L);
    }

    @Test
    void updateTenantOwner_shouldSetOwnerIdAndSave() {
        Tenant existing = Tenant.builder()
                .id(1L)
                .name("Tenant One")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(existing));

        tenantService.updateTenantOwner(1L, 9L);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant saved = captor.getValue();

        assertThat(saved.getOwnerId()).isEqualTo(9L);
    }

    @Test
    void updateTenantOwner_shouldThrowException_WhenTenantNotFound() {
        when(tenantRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.updateTenantOwner(2L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tenant not found with id: 2.");

        verify(tenantRepository).findById(2L);
        verify(tenantRepository, never()).save(any());
    }
}
