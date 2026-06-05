package ai.leadplus.api.v1.promptspecifications;


import ai.leadplus.application.promptspecifications.PromptSpecificationDto;
import ai.leadplus.application.promptspecifications.PromptSpecificationService;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/prompt-specifications")
@RequiredArgsConstructor
@Tag(name = "Prompt Specification", description = "Prompt Specification for Configurable Prompts")
public class PromptSpecificationController {
    private final PromptSpecificationService promptSpecificationService;

    @Operation(summary = "Get Latest Prompt Specification By Type")
    @GetMapping
    public ResponseEntity<PromptSpecificationResponse> getLatestPromptSpecification(@RequestParam PromptSpecificationType type) {
        log.info("[GET] Received request to get latest prompt specification for type: {}", type);
        PromptSpecificationDto promptSpecificationDto = promptSpecificationService.getLatestPromptSpecificationByType(type);
        PromptSpecificationResponse promptSpecificationResponse = PromptSpecificationResponse.fromDto(promptSpecificationDto);
        log.info("[GET] Returning latest prompt specification for type: {}", type);
        return ResponseEntity.ok(promptSpecificationResponse);
    }

    @Operation(summary = "Update Prompt Specification By Type")
    @PutMapping("/update")
    public ResponseEntity<PromptSpecificationResponse> updatePromptSpecification(@RequestBody PromptSpecificationRequest promptSpecificationRequest) {
        log.info("[PUT] Received request to update prompt specification for type: {}", promptSpecificationRequest.getType());
        PromptSpecificationDto promptSpecificationDto = promptSpecificationService.updatePromptSpecification(promptSpecificationRequest.toDto());
        PromptSpecificationResponse promptSpecificationResponse = PromptSpecificationResponse.fromDto(promptSpecificationDto);
        log.info("[PUT] Updated prompt specification for type: {}", promptSpecificationRequest.getType());
        return ResponseEntity.ok(promptSpecificationResponse);
    }

}
