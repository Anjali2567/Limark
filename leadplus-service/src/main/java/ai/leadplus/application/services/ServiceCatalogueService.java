package ai.leadplus.application.services;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.servicecategories.ServiceCategoryService;
import ai.leadplus.domain.industryservices.IndustryServiceMapping;
import ai.leadplus.domain.industryservices.IndustryServiceRepository;
import ai.leadplus.domain.services.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceCatalogueService {

    private final ServiceRepository serviceRepository;
    private final IndustryServiceRepository industryServiceRepository;
    private final ServiceCategoryService serviceCategoryService;

    public ServiceDto createService(ServiceDto dto) {
        serviceCategoryService.validateServiceCategoryId(dto.getServiceCategoryId());
        String slug = generateSlug(dto.getName());
        if (serviceRepository.findBySlugAndActiveTrue(slug).isPresent()) {
            throw new BadRequestException("Duplicate service: " + dto.getName());
        }
        dto.setSlug(slug);
        dto.setActive(true);
        dto.setDisabled(false);
        return ServiceDto.fromEntity(serviceRepository.save(dto.toEntity()));
    }

    public ServiceDto getById(String id) {
        return ServiceDto.fromEntity(findActiveById(id));
    }

    public Page<ServiceDto> getAll(Pageable pageable) {
        return serviceRepository.findAllByActiveTrue(pageable).map(ServiceDto::fromEntity);
    }

    public List<ServiceDto> getAllActive() {
        return serviceRepository.findAllByDisabledFalseAndActiveTrue()
                .stream()
                .map(ServiceDto::fromEntity)
                .toList();
    }

    public List<ServiceDto> getAllActiveByCategory(Long serviceCategoryId) {
        return serviceRepository.findAllByServiceCategoryIdAndDisabledFalseAndActiveTrue(serviceCategoryId)
                .stream()
                .map(ServiceDto::fromEntity)
                .toList();
    }

    public List<ServiceDto> getAllActiveByIndustryId(Long industryId) {
        List<Long> serviceIds = industryServiceRepository.findAllByIndustryId(industryId)
                .stream()
                .map(IndustryServiceMapping::getServiceId)
                .toList();
        if (CollectionUtils.isEmpty(serviceIds)) {
            return List.of();
        }
        return serviceRepository.findAllByIdInAndDisabledFalseAndActiveTrue(serviceIds)
                .stream()
                .map(ServiceDto::fromEntity)
                .toList();
    }

    public List<ServiceDto> getActiveServices(Long industryId, Long serviceCategoryId, String query) {
        List<ServiceDto> services;
        if (industryId != null) {
            services = getAllActiveByIndustryId(industryId);
        } else if (serviceCategoryId != null) {
            services = getAllActiveByCategory(serviceCategoryId);
        } else {
            services = getAllActive();
        }
        if (!StringUtils.hasText(query)) {
            return services;
        }
        String lowerQuery = query.trim().toLowerCase();
        return services.stream()
                .filter(s -> s.getName().toLowerCase().contains(lowerQuery))
                .toList();
    }

    public Map<Long, ServiceDto> getAllServicesAsMap(List<Long> ids) {
        return serviceRepository.findAllByIdInAndDisabledFalseAndActiveTrue(ids)
                .stream()
                .map(ServiceDto::fromEntity)
                .collect(Collectors.toMap(ServiceDto::getId, dto -> dto));
    }

    public ServiceDto updateService(String id, ServiceDto dto) {
        serviceCategoryService.validateServiceCategoryId(dto.getServiceCategoryId());
        ai.leadplus.domain.services.Service existing = findActiveById(id);
        existing.setName(dto.getName());
        existing.setServiceCategoryId(dto.getServiceCategoryId());
        existing.setDisabled(dto.isDisabled());

        String newSlug = generateSlug(dto.getName());
        if (!newSlug.equals(existing.getSlug())) {
            serviceRepository.findBySlugAndActiveTrue(newSlug)
                    .filter(s -> !s.getId().equals(existing.getId()))
                    .ifPresent(s -> {
                        throw new BadRequestException("Duplicate slug: " + newSlug);
                    });

            existing.setSlug(newSlug);
        }

        return ServiceDto.fromEntity(serviceRepository.save(existing));
    }

    public ServiceDto updateDisableStatus(String id, boolean disabled) {
        ai.leadplus.domain.services.Service existing = findActiveById(id);
        if (existing.isDisabled() == disabled) {
            return ServiceDto.fromEntity(existing);
        }
        existing.setDisabled(disabled);
        return ServiceDto.fromEntity(serviceRepository.save(existing));
    }

    public void validateServiceId(Long serviceId) {
        if (serviceId == null || !serviceRepository.existsByIdAndActiveTrueAndDisabledFalse(serviceId)) {
            throw new BadRequestException("Invalid Service ID: " + serviceId);
        }
    }

    public void validateServiceIds(List<Long> serviceIds) {
        if (CollectionUtils.isEmpty(serviceIds)) return;
        serviceIds = serviceIds.stream().distinct().toList();
        long count = serviceRepository.countAllByIdInAndDisabledFalseAndActiveTrue(serviceIds);
        if (count != serviceIds.size()) {
            throw new BadRequestException("One or more service IDs are invalid");
        }
    }

    public List<String> getAllActiveServiceNames() {
        return serviceRepository.findAllByDisabledFalseAndActiveTrue()
                .stream()
                .map(ai.leadplus.domain.services.Service::getName)
                .toList();
    }

    public List<Long> findIdsByNames(List<String> names) {
        return serviceRepository.findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(names)
                .stream()
                .map(ai.leadplus.domain.services.Service::getId)
                .toList();
    }

    public void deleteService(String id) {
        ai.leadplus.domain.services.Service existing = findActiveById(id);
        existing.setActive(false);
        serviceRepository.save(existing);
    }

    private ai.leadplus.domain.services.Service findActiveById(String id) {
        return serviceRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }

    private String generateSlug(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Service name must not be blank");
        }
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return slug;
    }
}
