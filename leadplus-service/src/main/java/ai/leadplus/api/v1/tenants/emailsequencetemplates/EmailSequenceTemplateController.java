package ai.leadplus.api.v1.tenants.emailsequencetemplates;

import ai.leadplus.api.common.TenantValidator;
import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateDto;
import ai.leadplus.application.emailsequencetemplates.EmailSequenceTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tenants/{tenantId}/sequence-templates")
@Tag(name = "Email Sequence Template APIs", description = "APIs for Email Sequence Template management")
public class EmailSequenceTemplateController {

    private final EmailSequenceTemplateService emailSequenceTemplateService;
    private final TenantValidator tenantValidator;

    @Operation(summary = "List all email sequence templates for a tenant")
    @GetMapping
    public ResponseEntity<List<EmailSequenceTemplateResponse>> listTemplates(@PathVariable Long tenantId) {
        tenantValidator.validateAuthenticatedUserByTenantId(tenantId);
        log.info("[GET] Listing email sequence templates for tenantId: {}", tenantId);
        List<EmailSequenceTemplateResponse> response = emailSequenceTemplateService.listTemplates(tenantId).stream()
                .map(EmailSequenceTemplateResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved Email sequence templates list for tenantId: {}", tenantId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get email sequence template by ID")
    @GetMapping("/{templateId}")
    public ResponseEntity<EmailSequenceTemplateResponse> getTemplate(
            @PathVariable Long tenantId,
            @PathVariable Long templateId) {
        tenantValidator.validateAuthenticatedUserByTenantId(tenantId);
        log.info("[GET] Getting email sequence template id: {}", templateId);
        EmailSequenceTemplateDto dto = emailSequenceTemplateService.getTemplate(tenantId, templateId);
        EmailSequenceTemplateResponse response = EmailSequenceTemplateResponse.fromDto(dto);
        log.info("[GET] Retrieved email sequence template response: {}", response);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new email sequence template")
    @PostMapping
    public ResponseEntity<EmailSequenceTemplateResponse> createTemplate(
            @PathVariable Long tenantId,
            @Valid @RequestBody EmailSequenceTemplateRequest request) {
        Long createdBy = tenantValidator.getAuthenticatedUserIdByTenantId(tenantId);
        log.info("[POST] Creating email sequence template for tenantId: {}", tenantId);
        EmailSequenceTemplateDto dto = emailSequenceTemplateService.createTemplate(tenantId, createdBy, request.toDto());
        log.info("[POST] Email sequence template created with id: {}", dto.getId());
        return ResponseEntity.ok(EmailSequenceTemplateResponse.fromDto(dto));
    }
}
