package ai.leadplus.api.v1.specificationcategories;

import ai.leadplus.application.specificationcategories.SpecificationCategoryService;
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
@RequestMapping("/v1/specification-categories")
@RequiredArgsConstructor
@Tag(name = "Specification Category", description = "Public Specification Category APIs")
public class SpecificationCategoryController {

    private final SpecificationCategoryService specificationCategoryService;

    @GetMapping
    @Operation(summary = "Get all active SpecificationCategories")
    public ResponseEntity<List<SpecificationCategoryResponse>> getAllSpecificationCategories() {
        log.info("[GET] Received request to get all active SpecificationCategories");
        List<SpecificationCategoryResponse> response = specificationCategoryService.getAllActive()
                .stream()
                .map(SpecificationCategoryResponse::fromDto)
                .toList();
        log.info("[GET] Retrieved {} SpecificationCategories", response.size());
        return ResponseEntity.ok(response);
    }
}
