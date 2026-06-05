package ai.leadplus.api.v1;

import ai.leadplus.api.v1.admin.industries.IndustryResponse;
import ai.leadplus.application.industries.IndustryDto;
import ai.leadplus.application.industries.IndustryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/industries")
@RequiredArgsConstructor
@Tag(name = "Industry", description = "Industry APIs")
public class IndustryController {

    private final IndustryService industryService;

    @Operation(summary = "Get All Industries")
    @GetMapping
    public ResponseEntity<Page<IndustryResponse>> getAllIndustries(@RequestParam(required = false) String query, Pageable pageable) {
        log.info("[GET] Received request to get all Industries");
        Page<IndustryDto> industryDtoPage = industryService.getAllIndustries(query, pageable);
        Page<IndustryResponse> responsePage = industryDtoPage.map(IndustryResponse::fromDto);
        log.info("[GET] Retrieved {} Industries", responsePage.getNumberOfElements());
        return ResponseEntity.ok(responsePage);
    }
}
