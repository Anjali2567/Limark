package ai.leadplus.api.v1.requestforquotes;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.api.common.VendorValidator;
import ai.leadplus.application.quotations.QuotationService;
import ai.leadplus.application.requestforquotes.RequestForQuoteDto;
import ai.leadplus.application.requestforquotes.RequestForQuoteService;
import ai.leadplus.application.requestforquotes.RequestForQuoteDetailedDto;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.domain.common.SourcingRequestType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/request-for-quotes")
@RequiredArgsConstructor
@Tag(name = "Request For Quote APIs", description = "APIs for managing Request For Quotes")
public class RequestForQuoteController {

    private final RequestForQuoteService requestForQuoteService;
    private final QuotationService quotationService;
    private final UserValidator userValidator;
    private final VendorValidator vendorValidator;

    @Operation(summary = "Create New Request For Quote")
    @PostMapping
    public ResponseEntity<RequestForQuoteResponse> createRequestForQuote(
            @Valid @RequestBody RequestForQuoteRequest request) {
        log.info("[POST] Received request to create Request For Quote");
        String userId = userValidator.getAuthenticatedUserId();
        RequestForQuoteDto dto = requestForQuoteService.createRequestForQuote(request.toDto(Long.valueOf(userId)));
        RequestForQuoteResponse response = RequestForQuoteResponse.fromDto(dto);
        log.info("[POST] Created Request For Quote with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Request For Quote by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RequestForQuoteDetailedResponse> getRequestForQuoteById(@PathVariable Long id) {

        log.info("[GET] Received request to get Request For Quote by ID: {}", id);

        RequestForQuoteDetailedDto dto =
                requestForQuoteService.getRequestForQuoteById(id);

        int numberOfVendorsForRfq =
                dto.getVendorIds() != null ? dto.getVendorIds().size() : 0;

        int numberOfQuotationsForRfq =
                quotationService.getCountOfQuotationsForSource(
                        dto.getId(),
                        SourcingRequestType.REQUEST_FOR_QUOTE
                );

        RequestForQuoteDetailedResponse requestForQuoteResponse =
                RequestForQuoteDetailedResponse.fromDto(
                        dto,
                        numberOfQuotationsForRfq,
                        numberOfVendorsForRfq
                );

        log.info("[GET] Retrieved Request For Quote with ID: {}", id);

        return ResponseEntity.ok(requestForQuoteResponse);
    }

    @Operation(summary = "Get All Request For Quotes by User")
    @GetMapping
    public ResponseEntity<Page<RequestForQuoteDetailedResponse>> getAllRequestForQuotesByUser(Pageable pageable) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[GET] Received request to get all Request For Quotes for userId: {}", userId);

        Page<RequestForQuoteDetailedDto> requestForQuoteDetailedDtos =
                requestForQuoteService.getAllRequestForQuotesByUser(Long.valueOf(userId), pageable);

        Page<RequestForQuoteDetailedResponse> requestForQuoteResponses =
                requestForQuoteDetailedDtos.map(dto -> {

                    int numberOfVendorsForRfq =
                            dto.getVendorIds() != null ? dto.getVendorIds().size() : 0;

                    int numberOfQuotationsForRfq =
                            quotationService.getCountOfQuotationsForSource(
                                    dto.getId(),
                                    SourcingRequestType.REQUEST_FOR_QUOTE
                            );

                    return RequestForQuoteDetailedResponse.fromDto(
                            dto,
                            numberOfQuotationsForRfq,
                            numberOfVendorsForRfq
                    );
                });

        log.info("[GET] Returning Request For Quotes for userId: {}", userId);
        return ResponseEntity.ok(requestForQuoteResponses);
    }

    @Operation(summary = "Upload Request For Quote File")
    @PutMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestForQuoteDetailedResponse> uploadRequestForQuoteFile(
            @PathVariable Long id,
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        log.info("[PUT] Received request to upload file for Request For Quote with id={}", id);
        RequestForQuoteDetailedDto updatedShowcase = requestForQuoteService.uploadAttachment(id, file);
        RequestForQuoteDetailedResponse response = RequestForQuoteDetailedResponse.fromDto(updatedShowcase);
        log.info("[PUT] Uploaded file for Request For Quote with id={}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Request For Quote File")
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<RequestForQuoteDetailedResponse> deleteVendorShowcaseFile(
            @PathVariable Long id,
            @PathVariable String attachmentId) {
        log.info("[DELETE] Received request to delete file with id={} for Request For Quote with id={} ", attachmentId, id);
        RequestForQuoteDetailedDto requestForQuoteDetailedDto
                = requestForQuoteService.deleteAttachment(id, attachmentId);
        RequestForQuoteDetailedResponse response = RequestForQuoteDetailedResponse.fromDto(requestForQuoteDetailedDto);
        log.info("[DELETE] Deleted file with id={} for vendor Request For Quote with id={}", attachmentId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Request For Quote")
    @PutMapping("/{id}")
    public ResponseEntity<RequestForQuoteDetailedResponse> updateRequestForQuote(
            @PathVariable Long id,
            @Valid @RequestBody RequestForQuoteRequest request) {
        log.info("[PUT] Received request to Update Request For Quote");
        RequestForQuoteDetailedDto dto = requestForQuoteService.updateRequestForQuote(id, request.toDto());
        RequestForQuoteDetailedResponse response = RequestForQuoteDetailedResponse.fromDto(dto);
        log.info("[PUT] Update Request For Quote with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Request For Quote by ID (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequestForQuote(@PathVariable Long id) {
        log.info("[DELETE] Received request to delete Request For Quote by ID: {}", id);
        requestForQuoteService.deleteRequestForQuote(id);
        log.info("[DELETE] Deleted Request For Quote with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get All Request For Quotes for Authenticated Vendor")
    @GetMapping("/vendors")
    public ResponseEntity<Page<RequestForQuoteDetailedResponse>> getAllRequestForQuotesByVendorId(Pageable pageable) {

        VendorDto vendorDto = vendorValidator.getAuthenticatedVendor();
        log.info("[GET] Received request to get all Request For Quotes for vendorId: {}", vendorDto.getId());

        Page<RequestForQuoteDetailedResponse> requestForQuoteResponses =
                requestForQuoteService
                        .getAllRequestForQuoteByVendor(vendorDto.getId(), pageable)
                        .map(dto -> {

                            int numberOfVendorsForRfq =
                                    dto.getVendorIds() != null ? dto.getVendorIds().size() : 0;

                            int numberOfQuotationsForRfq =
                                    quotationService.getCountOfQuotationsForSource(
                                            dto.getId(),
                                            SourcingRequestType.REQUEST_FOR_QUOTE
                                    );

                            return RequestForQuoteDetailedResponse.fromDto(
                                    dto,
                                    numberOfQuotationsForRfq,
                                    numberOfVendorsForRfq
                            );
                        });

        log.info("[GET] Returning Request For Quotes for vendorId: {}", vendorDto.getId());
        return ResponseEntity.ok(requestForQuoteResponses);
    }
}
