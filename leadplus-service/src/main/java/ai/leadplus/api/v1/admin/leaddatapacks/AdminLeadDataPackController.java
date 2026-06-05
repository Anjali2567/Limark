package ai.leadplus.api.v1.admin.leaddatapacks;

import ai.leadplus.api.v1.leaddatapacks.LeadDataPackResponse;
import ai.leadplus.application.leaddatapacks.LeadDataPackDto;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/lead-data-packs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Lead Data Packs")
public class AdminLeadDataPackController {

    private final LeadDataPackService leadDataPackService;

    @PostMapping
    @Operation(summary = "Create Lead Data Pack")
    public ResponseEntity<LeadDataPackResponse> createLeadDataPack(@Valid @RequestBody LeadDataPackRequest request) {
        log.info("[POST] Received request to create Lead Data Pack with name: {}", request.getName());
        LeadDataPackDto dto = leadDataPackService.createLeadDataPack(request.toDto());
        LeadDataPackResponse leadDataPackResponse = LeadDataPackResponse.fromDto(dto);
        log.info("[POST] Created Lead Data Pack with id: {}", dto.getId());
        return new ResponseEntity<>(leadDataPackResponse,HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Lead Data Pack")
    public ResponseEntity<LeadDataPackResponse> updateLeadDataPack(
            @PathVariable String id,
            @Valid @RequestBody LeadDataPackRequest request) {
        log.info("[PUT] Received request to update Lead Data Pack with id: {}", id);
        LeadDataPackDto leadDataPack = leadDataPackService.updateLeadDataPack(id, request.toDto());
        LeadDataPackResponse leadDataPackResponse = LeadDataPackResponse.fromDto(leadDataPack);
        log.info("[PUT] Updated Lead Data Pack with id: {}", id);
        return ResponseEntity.ok(leadDataPackResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Lead Data Pack")
    public ResponseEntity<Void> deleteLeadDataPackById(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Lead Data Pack with ID: {}", id);
        leadDataPackService.deleteLeadDataPackById(id);
        log.info("[DELETE] Deleted Lead Data Pack with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}