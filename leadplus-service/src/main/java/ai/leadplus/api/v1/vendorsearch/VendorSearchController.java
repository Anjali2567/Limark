package ai.leadplus.api.v1.vendorsearch;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.vendorqueryparser.VendorQueryParser;
import ai.leadplus.application.vendorqueryparser.VendorSearchParseDto;
import ai.leadplus.application.vendors.VendorDetailDto;
import ai.leadplus.application.vendors.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Search", description = "Endpoints for searching vendors based on various criteria")
public class VendorSearchController {

    private final VendorService vendorService;
    private final UserValidator userValidator;
    private final Optional<VendorQueryParser> vendorQueryParser;

    @Operation(summary = "Search vendors based on criteria")
    @PostMapping("/search")
    public ResponseEntity<Page<VendorDetailResponse>> searchVendors(
            @RequestBody(required = false) VendorSearchRequest request,
            Pageable pageable
    ) {
        log.info("[POST] Received request to search vendors");
        VendorSearchRequest searchRequest = request != null ? request : new VendorSearchRequest();
        String userId = userValidator.getAuthenticatedUserId();
        boolean isUserLoggedIn = !userId.equalsIgnoreCase("anonymousUser");
        Pageable searchPageable = isUserLoggedIn ? pageable : PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<VendorDetailDto> vendorPage = vendorService.searchVendors(
                searchRequest.getIndustryIds(),
                searchRequest.getServiceIds(),
                searchRequest.getSpecificationIds(),
                searchRequest.getCertifications(),
                searchPageable
        );
        Page<VendorDetailResponse> responsePage = vendorPage.map(isUserLoggedIn ? VendorDetailResponse::fromDto : VendorDetailResponse::fromDtoToAnonymous);
        log.info("[POST] Returning {} vendors for search criteria", responsePage.getNumberOfElements());
        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Parse vendor search query with AI")
    @PostMapping("/search/parse")
    public ResponseEntity<VendorSearchParseResponse> parseVendorQuery(@Valid @RequestBody VendorSearchParseRequest request) {
        log.info("[POST] Received vendor search query to parse: {}", request.getPrompt());
        VendorSearchParseDto parseDto = vendorQueryParser.orElseThrow(() -> new RuntimeException("Vendor query parser AI service is not available")).parseQuery(request.getPrompt());
        VendorSearchParseResponse response = VendorSearchParseResponse.fromDto(parseDto);
        log.info("[POST] Successfully parsed vendor search query");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get vendor profile by ID")
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorDetailResponse> getVendorById(@PathVariable Long vendorId) {
        log.info("[GET] Received request for vendor profile: {}", vendorId);
        VendorDetailDto dto = vendorService.getVendorDetailById(vendorId);
        log.info("[GET] Successfully retrieved vendor profile for vendorId: {}", vendorId);
        return ResponseEntity.ok(VendorDetailResponse.fromDto(dto));
    }
}
