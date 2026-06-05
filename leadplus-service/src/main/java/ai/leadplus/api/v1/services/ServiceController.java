package ai.leadplus.api.v1.services;

import ai.leadplus.application.services.ServiceCatalogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/services")
@RequiredArgsConstructor
@Tag(name = "Service", description = "Public Service APIs")
public class ServiceController {

    private final ServiceCatalogueService serviceCatalogueService;

    @GetMapping
    @Operation(summary = "Get active Services, optionally filtered by serviceCategoryId, industryId, or query")
    public ResponseEntity<List<ServiceResponse>> getServices(
            @RequestParam(required = false) Long serviceCategoryId,
            @RequestParam(required = false) Long industryId,
            @RequestParam(required = false) String query) {
        log.info("[GET] Received request to get Services, serviceCategoryId: {}, industryId: {}, query: {}", serviceCategoryId, industryId, query);
        List<ServiceResponse> response = serviceCatalogueService.getActiveServices(industryId, serviceCategoryId, query)
                .stream()
                .map(ServiceResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} Services", response.size());
        return ResponseEntity.ok(response);
    }
}
