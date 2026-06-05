package ai.leadplus.api.v1.admin.servicecategories;

import ai.leadplus.application.servicecategories.ServiceCategoryDto;
import ai.leadplus.application.servicecategories.ServiceCategoryService;
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
@RequestMapping("/v1/admin/service-categories")
@RequiredArgsConstructor
@Tag(name = "Service Category", description = "Service Category management APIs for admin")
public class AdminServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    @Operation(summary = "Create a new ServiceCategory")
    @PostMapping
    public ResponseEntity<ServiceCategoryResponse> createServiceCategory(@RequestBody @Valid ServiceCategoryRequest request) {
        log.info("[POST] Received request to create ServiceCategory with name: {}", request.getName());
        ServiceCategoryDto dto = serviceCategoryService.createServiceCategory(request.toDto());
        log.info("[POST] Successfully created ServiceCategory with id: {}", dto.getId());
        return new ResponseEntity<>(ServiceCategoryResponse.fromDto(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all ServiceCategories (paginated)")
    @GetMapping
    public ResponseEntity<Page<ServiceCategoryResponse>> getAllServiceCategories(Pageable pageable) {
        log.info("[GET] Received request to get all ServiceCategories");
        Page<ServiceCategoryResponse> page = serviceCategoryService.getAll(pageable).map(ServiceCategoryResponse::fromDto);
        log.info("[GET] Returning ServiceCategories page {} with {} elements", page.getNumber(), page.getNumberOfElements());
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get ServiceCategory by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryResponse> getServiceCategoryById(@PathVariable String id) {
        log.info("[GET] Received request to get ServiceCategory by id: {}", id);
        ServiceCategoryResponse response = ServiceCategoryResponse.fromDto(serviceCategoryService.getById(id));
        log.info("[GET] Returning ServiceCategory with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update ServiceCategory by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceCategoryResponse> updateServiceCategory(
            @PathVariable String id, @RequestBody @Valid ServiceCategoryRequest request) {
        log.info("[PUT] Received request to update ServiceCategory with id: {}", id);
        ServiceCategoryDto dto = serviceCategoryService.updateServiceCategory(id, request.toDto());
        log.info("[PUT] Successfully updated ServiceCategory with id: {}", id);
        return ResponseEntity.ok(ServiceCategoryResponse.fromDto(dto));
    }

    @Operation(summary = "Delete ServiceCategory by ID (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceCategory(@PathVariable String id) {
        log.info("[DELETE] Received request to delete ServiceCategory with id: {}", id);
        serviceCategoryService.deleteServiceCategory(id);
        log.info("[DELETE] Successfully deleted ServiceCategory with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
