package ai.leadplus.api.v1.admin.industries;

import ai.leadplus.application.industries.IndustryDto;
import ai.leadplus.application.industries.IndustryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/admin/industries")
@RequiredArgsConstructor
@Tag(name = "Industry", description = "Industry management APIs for admin")
public class AdminIndustryController {

    private final IndustryService industryService;

    @Operation(summary = "Create a new Industry")
    @PostMapping
    public ResponseEntity<IndustryResponse> createIndustry(@RequestBody @Valid IndustryRequest request) {
        log.info("[POST] Received request to create Industry with name: {}", request.getName());
        IndustryDto industryDto = industryService.createIndustry(request.toDto());
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[POST] Created Industry with id: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all Industries")
    @GetMapping
    public ResponseEntity<Page<IndustryResponse>> getAllIndustries(@RequestParam(required = false) String query, Pageable pageable) {
        log.info("[GET] Received request to get all Industries, query: {}", query);
        return ResponseEntity.ok(industryService.getAllAdminIndustries(query, pageable).map(IndustryResponse::fromDto));
    }

    @Operation(summary = "Get Industry by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<IndustryResponse> getIndustryBySlug(@PathVariable String slug) {
        log.info("[GET] Received request to get Industry by slug: {}", slug);
        return ResponseEntity.ok(IndustryResponse.fromDto(industryService.findBySlug(slug)));
    }

    @Operation(summary = "Get Industry by ID")
    @GetMapping("/{id}")
    public ResponseEntity<IndustryResponse> getIndustryById(@PathVariable String id) {
        log.info("[GET] Received request to get Industry by id: {}", id);
        IndustryDto industryDto = industryService.getIndustryById(id);
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[GET] Retrieved Industry with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Industry by ID")
    @PutMapping("/{id}")
    public ResponseEntity<IndustryResponse> updateIndustry(@PathVariable String id, @RequestBody @Valid IndustryRequest request) {
        log.info("[PUT] Received request to update Industry with id: {}", id);
        IndustryDto industryDto = industryService.updateIndustry(id, request.toDto());
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[PUT] Updated Industry with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Industry Image by ID")
    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IndustryResponse> uploadIndustryImageById(
            @PathVariable String id,
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        log.info("[PUT] Received request to upload Image for Industry with id: {}", id);
        IndustryDto industryDto = industryService.uploadIndustryImageById(id, file);
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[PUT] Uploaded Image for Industry with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Disable Industry by ID")
    @PutMapping("/{id}/disable")
    public ResponseEntity<IndustryResponse> disableIndustry(@PathVariable String id) {
        log.info("[PUT] Received request to disable Industry with id: {}", id);
        IndustryDto industryDto = industryService.updateDisableStatus(id, true);
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[PUT] Disabled Industry with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable Industry by ID")
    @DeleteMapping("/{id}/disable")
    public ResponseEntity<IndustryResponse> enableIndustry(@PathVariable String id) {
        log.info("[DELETE] Received request to enable Industry with id: {}", id);
        IndustryDto industryDto = industryService.updateDisableStatus(id, false);
        IndustryResponse response = IndustryResponse.fromDto(industryDto);
        log.info("[DELETE] Enabled Industry with id: {}", id);
        return ResponseEntity.ok(response);
    }
}
