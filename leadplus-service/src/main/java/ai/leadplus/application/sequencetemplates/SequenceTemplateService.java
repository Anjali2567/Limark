package ai.leadplus.application.sequencetemplates;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.sequencetemplates.SequenceTemplate;
import ai.leadplus.domain.sequencetemplates.SequenceTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SequenceTemplateService {
    private final SequenceTemplateRepository sequenceTemplateRepository;

    public SequenceTemplateDto createSequenceTemplate(SequenceTemplateDto sequenceTemplateDto) {
        SequenceTemplate sequenceTemplate = sequenceTemplateDto.toEntity();
        return SequenceTemplateDto.fromEntity(sequenceTemplateRepository.save(sequenceTemplate));
    }

    public SequenceTemplateDto getSequenceTemplateById(String id) {
        SequenceTemplate sequenceTemplate = findSequenceTemplateById(id);
        return SequenceTemplateDto.fromEntity(sequenceTemplate);
    }

    public SequenceTemplateDto updateSequenceTemplateById(String id, SequenceTemplateDto sequenceTemplateDto) {
        SequenceTemplate existingTemplate = findSequenceTemplateById(id);
        SequenceTemplate updatedTemplate = updateEntityFromDto(existingTemplate, sequenceTemplateDto);
        return SequenceTemplateDto.fromEntity(sequenceTemplateRepository.save(updatedTemplate));
    }

    public void deleteSequenceTemplateById(String id) {
        SequenceTemplate sequenceTemplate = findSequenceTemplateById(id);
        sequenceTemplateRepository.delete(sequenceTemplate);
    }

    private SequenceTemplate updateEntityFromDto(SequenceTemplate entity, SequenceTemplateDto dto) {
        entity.setName(dto.getName());
        entity.setStepCount(dto.getStepCount());
        entity.setDefaultDelays(dto.getDefaultDelays());
        entity.setPurpose(dto.getPurpose());
        return entity;
    }

    private SequenceTemplate findSequenceTemplateById(String id) {
        return sequenceTemplateRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("SequenceTemplate not found"));
    }
}
