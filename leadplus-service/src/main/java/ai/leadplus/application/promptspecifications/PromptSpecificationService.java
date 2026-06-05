package ai.leadplus.application.promptspecifications;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.promptspecifications.PromptSpecification;
import ai.leadplus.domain.promptspecifications.PromptSpecificationRepository;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptSpecificationService {

    private final PromptSpecificationRepository promptSpecificationRepository;

    public PromptSpecificationDto getLatestPromptSpecificationByType(PromptSpecificationType type) {
        PromptSpecification promptSpecification =
                promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(type);

        if (promptSpecification == null) {
            throw new ResourceNotFoundException("Prompt specification not found for type: " + type);
        }

        return PromptSpecificationDto.fromEntity(promptSpecification);
    }

    public PromptSpecificationDto updatePromptSpecification(PromptSpecificationDto dto) {
        PromptSpecification existing =
                promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(dto.getType());

        if (existing == null) {
            throw new ResourceNotFoundException("Prompt specification not found for type: " + dto.getType());
        }

        existing.setPromptTemplate(dto.getPromptTemplate());
        existing.setCreatedBy(dto.getCreatedBy());
        return PromptSpecificationDto.fromEntity(promptSpecificationRepository.save(existing));
    }
}
