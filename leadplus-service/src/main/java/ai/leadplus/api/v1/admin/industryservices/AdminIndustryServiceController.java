package ai.leadplus.api.v1.admin.industryservices;

import ai.leadplus.application.industryservices.IndustryServiceMappingService;
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
@RequestMapping("/v1/admin/industry-services")
@RequiredArgsConstructor
@Tag(name = "Industry Service", description = "Industry-Service mapping management APIs for admin")
public class AdminIndustryServiceController {

    private final IndustryServiceMappingService industryServiceMappingService;

    @Operation(summary = "Assign a Service to an Industry")
    @PostMapping
    public ResponseEntity<Void> assignServiceToIndustry(@RequestBody @Valid IndustryServiceRequest request) {
        log.info("[POST] Assigning service {} to industry {}", request.getServiceId(), request.getIndustryId());
        industryServiceMappingService.assignServiceToIndustry(request.getIndustryId(), request.getServiceId());
        log.info("[POST] Successfully assigned service {} to industry {}", request.getServiceId(), request.getIndustryId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a Service from an Industry")
    @DeleteMapping("/industries/{industryId}/services/{serviceId}")
    public ResponseEntity<Void> removeServiceFromIndustry(@PathVariable String industryId, @PathVariable String serviceId) {
        log.info("[DELETE] Removing service {} from industry {}", serviceId, industryId);
        industryServiceMappingService.removeServiceFromIndustry(Long.parseLong(industryId), Long.parseLong(serviceId));
        log.info("[DELETE] Successfully removed service {} from industry {}", serviceId, industryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Service IDs for an Industry")
    @GetMapping("/industries/{industryId}/services")
    public ResponseEntity<List<Long>> getServiceIdsByIndustry(@PathVariable String industryId) {
        log.info("[GET] Getting service IDs for industry: {}", industryId);
        List<Long> serviceIds = industryServiceMappingService.getServiceIdsByIndustry(Long.parseLong(industryId));
        log.info("[GET] Returning {} service IDs for industry: {}", serviceIds.size(), industryId);
        return ResponseEntity.ok(serviceIds);
    }

    @Operation(summary = "Get Industry IDs for a Service")
    @GetMapping("/services/{serviceId}/industries")
    public ResponseEntity<List<Long>> getIndustryIdsByService(@PathVariable String serviceId) {
        log.info("[GET] Getting industry IDs for service: {}", serviceId);
        List<Long> industryIds = industryServiceMappingService.getIndustryIdsByService(Long.parseLong(serviceId));
        log.info("[GET] Returning {} industry IDs for service: {}", industryIds.size(), serviceId);
        return ResponseEntity.ok(industryIds);
    }
}
