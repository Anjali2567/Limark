package ai.leadplus.api.v1.requestforproposal;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.requestforproposal.RequestForProposalDto;
import ai.leadplus.application.requestforproposal.RequestForProposalService;
import ai.leadplus.application.requestforproposal.RequestForProposalWithAttachmentsDto;
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
@RequestMapping("/v1/request-for-proposals")
@RequiredArgsConstructor
@Tag(name = "Request For Proposal APIs", description = "APIs for managing Request For Proposals")
public class RequestForProposalController {

    private final RequestForProposalService requestForProposalService;
    private final UserValidator userValidator;

    @Operation(summary = "Create New Request For Proposal")
    @PostMapping
    public ResponseEntity<RequestForProposalResponse> createRequestForProposal(
            @Valid @RequestBody RequestForProposalRequest request) {
        log.info("[POST] Received request to create Request For Proposal");
        String userId = userValidator.getAuthenticatedUserId();
        RequestForProposalDto dto = requestForProposalService.createRequestForProposal(request.toDto(Long.valueOf(userId)));
        RequestForProposalResponse response = RequestForProposalResponse.fromDto(dto);
        log.info("[POST] Created Request For Proposal with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Request For Proposal by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RequestForProposalWithAttachmentsResponse> getRequestForProposalById(@PathVariable Long id) {
        log.info("[GET] Received request to get Request For Proposal by ID: {}", id);
        RequestForProposalWithAttachmentsDto requestForProposalDto = requestForProposalService.getRequestForProposalById(id);
        RequestForProposalWithAttachmentsResponse requestForProposalResponse = RequestForProposalWithAttachmentsResponse.fromDto(requestForProposalDto);
        log.info("[GET] Retrieved Request For Proposal with ID: {}", id);
        return ResponseEntity.ok(requestForProposalResponse);
    }

    @Operation(summary = "Get All Request For Proposals by User")
    @GetMapping("/users")
    public ResponseEntity<Page<RequestForProposalWithAttachmentsResponse>> getAllRequestForProposals(Pageable pageable) {
        log.info("[GET] Received request to get all Request For Proposals");
        String userId = userValidator.getAuthenticatedUserId();
        Page<RequestForProposalWithAttachmentsDto> requestForProposalDtos = requestForProposalService.getAllRequestForProposalsByUser(Long.valueOf(userId), pageable);
        Page<RequestForProposalWithAttachmentsResponse> requestForProposalResponses = requestForProposalDtos.map(RequestForProposalWithAttachmentsResponse::fromDto);
        log.info("[GET] Returning Request For Proposals for userId: {}", userId);
        return ResponseEntity.ok(requestForProposalResponses);
    }

    @Operation(summary = "Update Request For Proposals")
    @PutMapping("/{id}")
    public ResponseEntity<RequestForProposalWithAttachmentsResponse> updateRequestForProposalById(
            @PathVariable Long id,
            @Valid @RequestBody RequestForProposalRequest request) {
        log.info("[PUT] Received request to Update Request For Proposal");
        RequestForProposalWithAttachmentsDto dto = requestForProposalService.updateRequestForProposal(id, request.toDto());
        RequestForProposalWithAttachmentsResponse response = RequestForProposalWithAttachmentsResponse.fromDto(dto);
        log.info("[PUT] Update Request For Proposal with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Request For Proposal File")
    @PutMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestForProposalWithAttachmentsResponse> uploadRequestForProposalFile(
            @PathVariable Long id,
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        log.info("[PUT] Received request to upload file for vendor Request For Proposal with id={}", id);
        RequestForProposalWithAttachmentsDto requestForProposalWithAttachmentsDto = requestForProposalService.uploadAttachment(id, file);
        RequestForProposalWithAttachmentsResponse response = RequestForProposalWithAttachmentsResponse.fromDto(requestForProposalWithAttachmentsDto);
        log.info("[PUT] Uploaded file for vendor Request For Proposal with id={} ", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Request For Proposal File")
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<RequestForProposalWithAttachmentsResponse> deleteVendorShowcaseFile(
            @PathVariable Long id,
            @PathVariable String attachmentId) {
        log.info("[DELETE] Received request to delete file with id={} for Request For Proposal with id={} ", attachmentId, id);
        RequestForProposalWithAttachmentsDto requestForProposalWithAttachmentsDto
                = requestForProposalService.deleteAttachment(id, attachmentId);
        RequestForProposalWithAttachmentsResponse response = RequestForProposalWithAttachmentsResponse.fromDto(requestForProposalWithAttachmentsDto);
        log.info("[DELETE] Deleted file with id={} for vendor Request For Proposal with id={}", attachmentId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Request For Proposal by ID (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequestForProposal(@PathVariable Long id) {
        log.info("[DELETE] Received request to delete Request For Proposal by ID: {}", id);
        requestForProposalService.deleteRequestForProposal(id);
        log.info("[DELETE] Deleted Request For Proposal with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
