package ai.leadplus.api.v1.admin.services;

import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.application.services.ServiceCatalogueService;
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
@RequestMapping("/v1/admin/services")
@RequiredArgsConstructor
@Tag(name = "Service", description = "Service management APIs for admin")
public class AdminServiceController {

    private final ServiceCatalogueService serviceCatalogueService;

    @Operation(summary = "Create a new Service")
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@RequestBody @Valid ServiceRequest request) {
        log.info("[POST] Received request to create Service with name: {}", request.getName());
        ServiceDto dto = serviceCatalogueService.createService(request.toDto());
        log.info("[POST] Successfully created Service with id: {}", dto.getId());
        return new ResponseEntity<>(ServiceResponse.fromDto(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all Services (paginated)")
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(Pageable pageable) {
        log.info("[GET] Received request to get all Services");
        Page<ServiceResponse> page = serviceCatalogueService.getAll(pageable).map(ServiceResponse::fromDto);
        log.info("[GET] Returning Services page {} with {} elements", page.getNumber(), page.getNumberOfElements());
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get Service by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable String id) {
        log.info("[GET] Received request to get Service by id: {}", id);
        ServiceResponse response = ServiceResponse.fromDto(serviceCatalogueService.getById(id));
        log.info("[GET] Returning Service with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Service by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String id, @RequestBody @Valid ServiceRequest request) {
        log.info("[PUT] Received request to update Service with id: {}", id);
        ServiceDto dto = serviceCatalogueService.updateService(id, request.toDto());
        log.info("[PUT] Successfully updated Service with id: {}", id);
        return ResponseEntity.ok(ServiceResponse.fromDto(dto));
    }

    @Operation(summary = "Delete Service by ID (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        log.info("[DELETE] Received request to delete Service with id: {}", id);
        serviceCatalogueService.deleteService(id);
        log.info("[DELETE] Successfully deleted Service with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disable Service by ID")
    @PutMapping("/{id}/disable")
    public ResponseEntity<ServiceResponse> disableService(@PathVariable String id) {
        log.info("[PUT] Received request to disable Service with id: {}", id);
        ServiceResponse response = ServiceResponse.fromDto(serviceCatalogueService.updateDisableStatus(id, true));
        log.info("[PUT] Successfully disabled Service with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable Service by ID")
    @DeleteMapping("/{id}/disable")
    public ResponseEntity<ServiceResponse> enableService(@PathVariable String id) {
        log.info("[DELETE] Received request to enable Service with id: {}", id);
        ServiceResponse response = ServiceResponse.fromDto(serviceCatalogueService.updateDisableStatus(id, false));
        log.info("[DELETE] Successfully enabled Service with id: {}", id);
        return ResponseEntity.ok(response);
    }
}
