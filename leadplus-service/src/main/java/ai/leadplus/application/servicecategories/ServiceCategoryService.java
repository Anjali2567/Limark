package ai.leadplus.application.servicecategories;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.servicecategories.ServiceCategory;
import ai.leadplus.domain.servicecategories.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryDto createServiceCategory(ServiceCategoryDto dto) {
        if (serviceCategoryRepository.existsByNameIgnoreCaseAndActiveTrue(dto.getName())) {
            throw new BadRequestException("Duplicate service category: " + dto.getName());
        }
        ServiceCategory entity = dto.toEntity();
        entity.setActive(true);
        return ServiceCategoryDto.fromEntity(serviceCategoryRepository.save(entity));
    }

    public ServiceCategoryDto getById(String id) {
        return ServiceCategoryDto.fromEntity(findActiveById(id));
    }

    public Page<ServiceCategoryDto> getAll(Pageable pageable) {
        return serviceCategoryRepository.findAllByActiveTrue(pageable).map(ServiceCategoryDto::fromEntity);
    }

    public List<ServiceCategoryDto> getAllActive() {
        return serviceCategoryRepository.findAllByActiveTrue()
                .stream()
                .map(ServiceCategoryDto::fromEntity)
                .toList();
    }

    public ServiceCategoryDto updateServiceCategory(String id, ServiceCategoryDto dto) {
        ServiceCategory existing = findActiveById(id);
        existing.setName(dto.getName());
        return ServiceCategoryDto.fromEntity(serviceCategoryRepository.save(existing));
    }

    public void validateServiceCategoryId(Long id) {
        if (!serviceCategoryRepository.existsByIdAndActiveTrue(id)) {
            throw new ResourceNotFoundException("Invalid serviceCategoryId: " + id);
        }
    }

    public void deleteServiceCategory(String id) {
        ServiceCategory existing = findActiveById(id);
        existing.setActive(false);
        serviceCategoryRepository.save(existing);
    }

    private ServiceCategory findActiveById(String id) {
        return serviceCategoryRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory not found with id: " + id));
    }
}
