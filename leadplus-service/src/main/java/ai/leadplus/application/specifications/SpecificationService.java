package ai.leadplus.application.specifications;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.specificationcategories.SpecificationCategoryService;
import ai.leadplus.domain.servicespecifications.ServiceSpecification;
import ai.leadplus.domain.servicespecifications.ServiceSpecificationRepository;
import ai.leadplus.domain.specifications.Specification;
import ai.leadplus.domain.specifications.SpecificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecificationService {

    private final SpecificationCategoryService specificationCategoryService;
    private final SpecificationRepository specificationRepository;
    private final ServiceSpecificationRepository serviceSpecificationRepository;

    public SpecificationDto createSpecification(SpecificationDto dto) {
        specificationCategoryService.validateSpecificationCategoryId(dto.getSpecificationCategoryId());
        Specification entity = dto.toEntity();
        entity.setActive(true);
        entity.setDisabled(false);
        return SpecificationDto.fromEntity(specificationRepository.save(entity));
    }

    public SpecificationDto getById(String id) {
        return SpecificationDto.fromEntity(findActiveById(id));
    }

    public List<SpecificationDto> getAll() {
        return specificationRepository.findAllByDisabledFalseAndActiveTrue()
                .stream()
                .map(SpecificationDto::fromEntity)
                .toList();
    }

    public List<SpecificationDto> getAllByCategory(Long specificationCategoryId) {
        if (specificationCategoryId == null) {
            return getAll();
        }
        return specificationRepository
                .findAllBySpecificationCategoryIdAndDisabledFalseAndActiveTrue(specificationCategoryId)
                .stream()
                .map(SpecificationDto::fromEntity)
                .toList();
    }

    public List<SpecificationDto> getAllByServiceId(Long serviceId) {
        List<Long> ids = serviceSpecificationRepository.findAllByServiceId(serviceId)
                .stream()
                .map(ServiceSpecification::getSpecificationId)
                .toList();
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return specificationRepository.findAllByIdInAndDisabledFalseAndActiveTrue(ids)
                .stream()
                .map(SpecificationDto::fromEntity)
                .toList();
    }

    public List<SpecificationDto> getActiveSpecifications(Long specificationCategoryId, Long serviceId) {
        if (serviceId != null) {
            return getAllByServiceId(serviceId);
        } else if (specificationCategoryId != null) {
            return getAllByCategory(specificationCategoryId);
        }
        return getAll();
    }

    public Map<Long, SpecificationDto> getAllSpecificationsAsMap(List<Long> ids) {
        return specificationRepository.findAllByIdInAndDisabledFalseAndActiveTrue(ids)
                .stream()
                .map(SpecificationDto::fromEntity)
                .collect(Collectors.toMap(SpecificationDto::getId, dto -> dto));
    }

    public SpecificationDto updateSpecification(String id, SpecificationDto dto) {
        specificationCategoryService.validateSpecificationCategoryId(dto.getSpecificationCategoryId());
        Specification existing = findActiveById(id);
        existing.setName(dto.getName());
        existing.setType(dto.getType());
        existing.setIcon(dto.getIcon());
        existing.setSpecificationCategoryId(dto.getSpecificationCategoryId());
        existing.setDisabled(dto.isDisabled());
        return SpecificationDto.fromEntity(specificationRepository.save(existing));
    }

    public SpecificationDto updateDisableStatus(String id, boolean disabled) {
        Specification existing = findActiveById(id);
        if (existing.isDisabled() == disabled) {
            return SpecificationDto.fromEntity(existing);
        }
        existing.setDisabled(disabled);
        return SpecificationDto.fromEntity(specificationRepository.save(existing));
    }

    public void validateSpecificationId(Long id) {
        if (id == null || !specificationRepository.existsByIdAndDisabledFalseAndActiveTrue(id)) {
            throw new BadRequestException("Invalid Specification ID: " + id);
        }
    }

    public void validateSpecificationIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        ids = ids.stream().distinct().toList();
        long count = specificationRepository.countAllByIdInAndDisabledFalseAndActiveTrue(ids);
        if (count != ids.size()) {
            throw new BadRequestException("One or more Specification IDs are invalid");
        }
    }

    private Specification findActiveById(String id) {
        return specificationRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Specification not found with id: " + id));
    }

    public void deleteSpecification(String id) {
        Specification existing = findActiveById(id);
        existing.setActive(false);
        specificationRepository.save(existing);
    }

    public List<String> getAllActiveSpecificationNames() {
        return specificationRepository.findAllByDisabledFalseAndActiveTrue()
                .stream().map(Specification::getName).toList();
    }

    public List<Long> findSpecificationIdsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) return List.of();
        return specificationRepository.findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(names)
                .stream().map(Specification::getId).toList();
    }
}
