package ai.leadplus.api.v1.servicecategories;

import ai.leadplus.application.servicecategories.ServiceCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/service-categories")
@RequiredArgsConstructor
@Tag(name = "Service Category", description = "Public Service Category APIs")
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    @GetMapping
    @Operation(summary = "Get all active ServiceCategories")
    public ResponseEntity<List<ServiceCategoryResponse>> getAllServiceCategories() {
        log.info("[GET] Received request to get all active ServiceCategories");
        List<ServiceCategoryResponse> response = serviceCategoryService.getAllActive()
                .stream()
                .map(ServiceCategoryResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} ServiceCategories", response.size());
        return ResponseEntity.ok(response);
    }
}
