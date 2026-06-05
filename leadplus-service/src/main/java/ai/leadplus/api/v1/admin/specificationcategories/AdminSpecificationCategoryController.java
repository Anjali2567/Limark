package ai.leadplus.api.v1.admin.specificationcategories;

import ai.leadplus.application.specificationcategories.SpecificationCategoryDto;
import ai.leadplus.application.specificationcategories.SpecificationCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/admin/specification-categories")
@RequiredArgsConstructor
@Tag(name = "Specification Category", description = "Specification Category management APIs for admin")
public class AdminSpecificationCategoryController {

    private final SpecificationCategoryService specificationCategoryService;

    @Operation(summary = "Create a new SpecificationCategory")
    @PostMapping
    public ResponseEntity<SpecificationCategoryResponse> createSpecificationCategory(
            @RequestBody @Valid SpecificationCategoryRequest request) {
        log.info("[POST] Received request to create SpecificationCategory with name: {}", request.getName());
        SpecificationCategoryDto dto = specificationCategoryService.createSpecificationCategory(request.toDto());
        log.info("[POST] Successfully created SpecificationCategory with id: {}", dto.getId());
        return new ResponseEntity<>(SpecificationCategoryResponse.fromDto(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all SpecificationCategories (paginated)")
    @GetMapping
    public ResponseEntity<Page<SpecificationCategoryResponse>> getAllSpecificationCategories(Pageable pageable) {
        log.info("[GET] Received request to get all SpecificationCategories");
        Page<SpecificationCategoryResponse> page = specificationCategoryService.getAll(pageable).map(SpecificationCategoryResponse::fromDto);
        log.info("[GET] Returning SpecificationCategories page {} with {} elements", page.getNumber(), page.getNumberOfElements());
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get SpecificationCategory by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SpecificationCategoryResponse> getSpecificationCategoryById(@PathVariable String id) {
        log.info("[GET] Received request to get SpecificationCategory by id: {}", id);
        SpecificationCategoryResponse response = SpecificationCategoryResponse.fromDto(specificationCategoryService.getById(id));
        log.info("[GET] Returning SpecificationCategory with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update SpecificationCategory by ID")
    @PutMapping("/{id}")
    public ResponseEntity<SpecificationCategoryResponse> updateSpecificationCategory(
            @PathVariable String id, @RequestBody @Valid SpecificationCategoryRequest request) {
        log.info("[PUT] Received request to update SpecificationCategory with id: {}", id);
        SpecificationCategoryDto dto = specificationCategoryService.updateSpecificationCategory(id, request.toDto());
        log.info("[PUT] Successfully updated SpecificationCategory with id: {}", id);
        return ResponseEntity.ok(SpecificationCategoryResponse.fromDto(dto));
    }

    @Operation(summary = "Delete SpecificationCategory by ID (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecificationCategory(@PathVariable String id) {
        log.info("[DELETE] Received request to delete SpecificationCategory with id: {}", id);
        specificationCategoryService.deleteSpecificationCategory(id);
        log.info("[DELETE] Successfully deleted SpecificationCategory with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
