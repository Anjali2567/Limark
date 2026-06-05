package ai.leadplus.api.v1.specifications;

import ai.leadplus.application.specifications.SpecificationService;
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
@RequestMapping("/v1/specifications")
@RequiredArgsConstructor
@Tag(name = "Specification", description = "Public Specification APIs")
public class SpecificationController {

    private final SpecificationService specificationService;

    @GetMapping
    @Operation(summary = "Get active Specifications, optionally filtered by specificationCategoryId or serviceId")
    public ResponseEntity<List<SpecificationResponse>> getSpecifications(
            @RequestParam(required = false) Long specificationCategoryId,
            @RequestParam(required = false) Long serviceId) {
        log.info("[GET] Received request to get Specifications, categoryId: {}, serviceId: {}", specificationCategoryId, serviceId);
        List<SpecificationResponse> response = specificationService
                .getActiveSpecifications(specificationCategoryId, serviceId)
                .stream()
                .map(SpecificationResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} Specifications", response.size());
        return ResponseEntity.ok(response);
    }
}
