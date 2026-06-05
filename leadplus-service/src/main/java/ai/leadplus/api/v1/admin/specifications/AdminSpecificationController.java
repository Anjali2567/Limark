package ai.leadplus.api.v1.admin.specifications;

import ai.leadplus.application.specifications.SpecificationDto;
import ai.leadplus.application.specifications.SpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/specifications")
@RequiredArgsConstructor
@Tag(name = "Specification", description = "Specification management APIs for admin")
public class AdminSpecificationController {

    private final SpecificationService specificationService;

    @Operation(summary = "Create a new Specification")
    @PostMapping
    public ResponseEntity<SpecificationResponse> createSpecification(
            @RequestBody @Valid SpecificationRequest request) {
        log.info("[POST] Received request to create Specification with name: {}", request.getName());
        SpecificationDto dto = specificationService.createSpecification(request.toDto());
        log.info("[POST] Successfully created Specification with id: {}", dto.getId());
        return new ResponseEntity<>(SpecificationResponse.fromDto(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get Specifications by category")
    @GetMapping
    public ResponseEntity<List<SpecificationResponse>> getSpecifications(
            @RequestParam(required = false) Long specificationCategoryId) {
        log.info("[GET] Received request to get Specifications, categoryId: {}", specificationCategoryId);
        List<SpecificationResponse> items = specificationService.getAllByCategory(specificationCategoryId)
                .stream().map(SpecificationResponse::fromDto).toList();
        log.info("[GET] Returning {} Specifications", items.size());
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Get Specification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SpecificationResponse> getSpecificationById(@PathVariable String id) {
        log.info("[GET] Received request to get Specification by id: {}", id);
        SpecificationResponse response = SpecificationResponse.fromDto(specificationService.getById(id));
        log.info("[GET] Returning Specification with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Specification by ID")
    @PutMapping("/{id}")
    public ResponseEntity<SpecificationResponse> updateSpecification(
            @PathVariable String id, @RequestBody @Valid SpecificationRequest request) {
        log.info("[PUT] Received request to update Specification with id: {}", id);
        SpecificationDto dto = specificationService.updateSpecification(id, request.toDto());
        log.info("[PUT] Successfully updated Specification with id: {}", id);
        return ResponseEntity.ok(SpecificationResponse.fromDto(dto));
    }

    @Operation(summary = "Disable Specification by ID")
    @PutMapping("/{id}/disable")
    public ResponseEntity<SpecificationResponse> disableSpecification(@PathVariable String id) {
        log.info("[PUT] Received request to disable Specification with id: {}", id);
        SpecificationResponse response = SpecificationResponse.fromDto(specificationService.updateDisableStatus(id, true));
        log.info("[PUT] Successfully disabled Specification with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable Specification by ID")
    @DeleteMapping("/{id}/disable")
    public ResponseEntity<SpecificationResponse> enableSpecification(@PathVariable String id) {
        log.info("[DELETE] Received request to enable Specification with id: {}", id);
        SpecificationResponse response = SpecificationResponse.fromDto(specificationService.updateDisableStatus(id, false));
        log.info("[DELETE] Successfully enabled Specification with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Specification by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecification(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Specification with id: {}", id);
        specificationService.deleteSpecification(id);
        log.info("[DELETE] Successfully deleted Specification with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
