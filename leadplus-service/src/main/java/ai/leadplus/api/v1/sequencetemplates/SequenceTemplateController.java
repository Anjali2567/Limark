package ai.leadplus.api.v1.sequencetemplates;

import ai.leadplus.application.sequencetemplates.SequenceTemplateDto;
import ai.leadplus.application.sequencetemplates.SequenceTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/sequence-templates")
@RequiredArgsConstructor
@Tag(name = "Sequence Templates", description = "APIs for Sequence Template Management")
public class SequenceTemplateController {
    private final SequenceTemplateService sequenceTemplateService;

    @Operation(summary = "Get Sequence Template by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SequenceTemplateResponse> getSequenceTemplateById(@PathVariable String id) {
        log.info("[GET] Received request to get Sequence Template by id: {}", id);
        SequenceTemplateDto sequenceTemplateDto = sequenceTemplateService.getSequenceTemplateById(id);
        SequenceTemplateResponse response = SequenceTemplateResponse.fromDto(sequenceTemplateDto);
        log.info("[GET] Retrieved Sequence Template with id: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Create a new Sequence Template")
    @PostMapping
    public ResponseEntity<SequenceTemplateResponse> createSequenceTemplate(
            @RequestBody SequenceTemplateRequest request) {
        log.info("[POST] Received request to create Sequence Template with name: {}", request.getName());
        SequenceTemplateDto sequenceTemplateDto = sequenceTemplateService.createSequenceTemplate(request.toDto());
        SequenceTemplateResponse response = SequenceTemplateResponse.fromDto(sequenceTemplateDto);
        log.info("[POST] Created Sequence Template with id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an existing Sequence Template by ID")
    @PutMapping("/{id}")
    public ResponseEntity<SequenceTemplateResponse> updateSequenceTemplate(
            @PathVariable String id,
            @RequestBody SequenceTemplateRequest request) {
        log.info("[PUT] Received request to update Sequence Template with id: {}", id);
        SequenceTemplateDto sequenceTemplateDto = sequenceTemplateService.updateSequenceTemplateById(id, request.toDto());
        SequenceTemplateResponse response = SequenceTemplateResponse.fromDto(sequenceTemplateDto);
        log.info("[PUT] Updated Sequence Template with id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a Sequence Template by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSequenceTemplate(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Sequence Template with id: {}", id);
        sequenceTemplateService.deleteSequenceTemplateById(id);
        log.info("[DELETE] Deleted Sequence Template with id: {}", id);
        return ResponseEntity.noContent().build();
    }

}
