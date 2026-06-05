package ai.leadplus.application.specificationcategories;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.specificationcategories.SpecificationCategory;
import ai.leadplus.domain.specificationcategories.SpecificationCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecificationCategoryService {

    private final SpecificationCategoryRepository specificationCategoryRepository;

    public SpecificationCategoryDto createSpecificationCategory(SpecificationCategoryDto dto) {
        SpecificationCategory entity = dto.toEntity();
        entity.setActive(true);
        return SpecificationCategoryDto.fromEntity(specificationCategoryRepository.save(entity));
    }

    public SpecificationCategoryDto getById(String id) {
        return SpecificationCategoryDto.fromEntity(findActiveById(id));
    }

    public Page<SpecificationCategoryDto> getAll(Pageable pageable) {
        return specificationCategoryRepository.findAllByActiveTrue(pageable).map(SpecificationCategoryDto::fromEntity);
    }

    public List<SpecificationCategoryDto> getAllActive() {
        return specificationCategoryRepository.findAllByActiveTrue()
                .stream()
                .map(SpecificationCategoryDto::fromEntity)
                .toList();
    }

    public SpecificationCategoryDto updateSpecificationCategory(String id, SpecificationCategoryDto dto) {
        SpecificationCategory existing = findActiveById(id);
        existing.setName(dto.getName());
        existing.setType(dto.getType());
        return SpecificationCategoryDto.fromEntity(specificationCategoryRepository.save(existing));
    }

    public void validateSpecificationCategoryId(Long id) {
        if (!specificationCategoryRepository.existsByIdAndActiveTrue(id)) {
            throw new ResourceNotFoundException("Invalid specificationCategoryId: " + id);
        }
    }

    public void deleteSpecificationCategory(String id) {
        SpecificationCategory existing = findActiveById(id);
        existing.setActive(false);
        specificationCategoryRepository.save(existing);
    }

    private SpecificationCategory findActiveById(String id) {
        return specificationCategoryRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("SpecificationCategory not found with id: " + id));
    }
}
