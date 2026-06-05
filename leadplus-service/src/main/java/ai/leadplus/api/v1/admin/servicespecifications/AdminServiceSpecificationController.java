package ai.leadplus.api.v1.admin.servicespecifications;

import ai.leadplus.application.servicespecifications.ServiceSpecificationService;
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
@RequestMapping("/v1/admin/service-specifications")
@RequiredArgsConstructor
@Tag(name = "Service Specification", description = "Service-Specification mapping management APIs for admin")
public class AdminServiceSpecificationController {

    private final ServiceSpecificationService serviceSpecificationService;

    @Operation(summary = "Assign a Specification to a Service")
    @PostMapping
    public ResponseEntity<Void> assignSpecificationToService(@RequestBody @Valid ServiceSpecificationRequest request) {
        log.info("[POST] Assigning specification {} to service {}", request.getSpecificationId(), request.getServiceId());
        serviceSpecificationService.assignSpecificationToService(request.getServiceId(), request.getSpecificationId());
        log.info("[POST] Successfully assigned specification {} to service {}", request.getSpecificationId(), request.getServiceId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Remove a Specification from a Service")
    @DeleteMapping("/services/{serviceId}/specifications/{specificationId}")
    public ResponseEntity<Void> removeSpecificationFromService(
            @PathVariable Long serviceId, @PathVariable Long specificationId) {
        log.info("[DELETE] Removing specification {} from service {}", specificationId, serviceId);
        serviceSpecificationService.removeSpecificationFromService(serviceId, specificationId);
        log.info("[DELETE] Successfully removed specification {} from service {}", specificationId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Specification IDs for a Service")
    @GetMapping("/services/{serviceId}/specifications")
    public ResponseEntity<List<Long>> getSpecificationIdsByService(@PathVariable Long serviceId) {
        log.info("[GET] Getting specification IDs for service: {}", serviceId);
        List<Long> specIds = serviceSpecificationService.getSpecificationIdsByService(serviceId);
        log.info("[GET] Returning {} specification IDs for service: {}", specIds.size(), serviceId);
        return ResponseEntity.ok(specIds);
    }
}
