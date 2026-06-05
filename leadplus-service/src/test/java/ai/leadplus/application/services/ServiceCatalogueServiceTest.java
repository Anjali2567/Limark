package ai.leadplus.application.services;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.servicecategories.ServiceCategoryService;
import ai.leadplus.domain.industryservices.IndustryServiceRepository;
import ai.leadplus.domain.services.Service;
import ai.leadplus.domain.services.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogueServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private IndustryServiceRepository industryServiceRepository;

    @Mock
    private ServiceCategoryService serviceCategoryService;

    @InjectMocks
    private ServiceCatalogueService serviceCatalogueService;

    private ServiceDto serviceDto;
    private Service service;

    @BeforeEach
    void setUp() {
        serviceDto = ServiceDto.builder()
                .id(1L)
                .name("Test Service")
                .serviceCategoryId(1L)
                .disabled(false)
                .build();

        service = Service.builder()
                .id(1L)
                .name("Test Service")
                .slug("test-service")
                .serviceCategoryId(1L)
                .disabled(false)
                .active(true)
                .build();
    }

    @Test
    void createService_ShouldReturnSavedService_WhenValid() {
        when(serviceRepository.findBySlugAndActiveTrue("test-service")).thenReturn(Optional.empty());
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceDto result = serviceCatalogueService.createService(serviceDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(serviceCategoryService).validateServiceCategoryId(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void createService_ShouldThrowBadRequestException_WhenCategoryInvalid() {
        doThrow(new BadRequestException("Invalid serviceCategoryId: cat1"))
                .when(serviceCategoryService).validateServiceCategoryId(1L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> serviceCatalogueService.createService(serviceDto)
        );

        assertTrue(exception.getMessage().contains("Invalid serviceCategoryId"));
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void createService_ShouldThrowBadRequestException_WhenNameBlank() {
        serviceDto.setName("  ");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> serviceCatalogueService.createService(serviceDto)
        );

        assertEquals("Service name must not be blank", exception.getMessage());
    }

    @Test
    void createService_ShouldThrowBadRequestException_WhenDuplicateSlug() {
        when(serviceRepository.findBySlugAndActiveTrue("test-service")).thenReturn(Optional.of(service));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> serviceCatalogueService.createService(serviceDto)
        );

        assertTrue(exception.getMessage().contains("Duplicate service"));
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnService_WhenFound() {
        when(serviceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(service));

        ServiceDto result = serviceCatalogueService.getById("1");

        assertNotNull(result);
        assertEquals("Test Service", result.getName());
    }

    @Test
    void getById_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(serviceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> serviceCatalogueService.getById("1")
        );

        assertEquals("Service not found with id: 1", exception.getMessage());
    }

    @Test
    void getAll_ShouldReturnPagedServices() {
        Page<Service> page = new PageImpl<>(List.of(service));
        Pageable pageable = PageRequest.of(0, 10);

        when(serviceRepository.findAllByActiveTrue(pageable)).thenReturn(page);

        Page<ServiceDto> result = serviceCatalogueService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Service", result.getContent().getFirst().getName());
    }

    @Test
    void getAllActive_ShouldReturnActiveServices() {
        when(serviceRepository.findAllByDisabledFalseAndActiveTrue()).thenReturn(List.of(service));

        List<ServiceDto> result = serviceCatalogueService.getAllActive();

        assertEquals(1, result.size());
        assertEquals("Test Service", result.getFirst().getName());
    }

    @Test
    void updateService_ShouldUpdateAndReturnService_WhenValid() {
        ServiceDto updateDto = ServiceDto.builder()
                .serviceCategoryId(1L)
                .name("Updated Service")
                .disabled(true)
                .build();

        when(serviceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(service));
        when(serviceRepository.findBySlugAndActiveTrue("updated-service")).thenReturn(Optional.empty());
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceDto result = serviceCatalogueService.updateService("1", updateDto);

        assertNotNull(result);
        verify(serviceCategoryService).validateServiceCategoryId(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void updateService_ShouldThrowBadRequestException_WhenCategoryInvalid() {
        ServiceDto updateDto = ServiceDto.builder()
                .serviceCategoryId(0L)
                .name("Some Service")
                .build();

        doThrow(new BadRequestException("Invalid serviceCategoryId: invalid"))
                .when(serviceCategoryService).validateServiceCategoryId(0L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> serviceCatalogueService.updateService("1", updateDto)
        );

        assertTrue(exception.getMessage().contains("Invalid serviceCategoryId"));
    }

    @Test
    void deleteService_ShouldSetActiveFalseAndSave() {
        when(serviceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(service));

        serviceCatalogueService.deleteService("1");

        assertFalse(service.isActive());
        verify(serviceRepository).save(service);
    }

    @Test
    void deleteService_ShouldThrowResourceNotFoundException_WhenNotFound() {
        when(serviceRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> serviceCatalogueService.deleteService("1")
        );

        assertEquals("Service not found with id: 1", exception.getMessage());
    }
}
