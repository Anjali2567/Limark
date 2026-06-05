package ai.leadplus.application.emailsequencetemplates;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.emailsequencetemplates.EmailSequenceTemplate;
import ai.leadplus.domain.emailsequencetemplates.EmailSequenceTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSequenceTemplateService {

    private final EmailSequenceTemplateRepository emailSequenceTemplateRepository;

    public List<EmailSequenceTemplateDto> listTemplates(Long tenantId) {
        return emailSequenceTemplateRepository.findAllByTenantId(tenantId).stream()
                .map(EmailSequenceTemplateDto::fromEntity)
                .toList();
    }

    public EmailSequenceTemplateDto getTemplate(Long tenantId, Long templateId) {
        EmailSequenceTemplate template = findByIdAndTenantId(tenantId, templateId);
        return EmailSequenceTemplateDto.fromEntity(template);
    }

    public EmailSequenceTemplateDto createTemplate(Long tenantId, Long createdBy, EmailSequenceTemplateDto dto) {
        if (dto.getSteps() == null || dto.getSteps().isEmpty()) {
            throw new BadRequestException("Template must have at least one step");
        }
        EmailSequenceTemplate template = dto.toEntity();
        template.setTenantId(tenantId);
        template.setCreatedBy(createdBy);
        return EmailSequenceTemplateDto.fromEntity(emailSequenceTemplateRepository.save(template));
    }

    private EmailSequenceTemplate findByIdAndTenantId(Long tenantId, Long templateId) {
        return emailSequenceTemplateRepository.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Email sequence template not found with id: " + templateId));
    }
}
