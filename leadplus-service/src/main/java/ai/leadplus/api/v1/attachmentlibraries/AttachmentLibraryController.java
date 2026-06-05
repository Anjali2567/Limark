package ai.leadplus.api.v1.attachmentlibraries;

import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import ai.leadplus.application.attachmentlibraries.AttachmentLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/attachment-libraries")
@RequiredArgsConstructor
@Tag(name = "Attachment Libraries", description = "APIs for lead agents to manage Attachment Libraries")
public class AttachmentLibraryController {

    private final AttachmentLibraryService attachmentLibraryService;

    @Operation(summary = "Search Attachment Libraries")
    @GetMapping
    public ResponseEntity<Page<AttachmentLibraryResponse>> searchAttachmentLibraries(@PathVariable Long tenantId,
                                                                                     @PathVariable String workspaceId,
                                                                                     @RequestParam(required = false) String query,
                                                                                     @RequestParam(required = false) List<String> fileTypes,
                                                                                     Pageable pageable) {
        log.info("[GET] Received request to Search Attachment Libraries with Query: {}", query);
        Page<AttachmentLibraryDto> attachmentLibraryDtoList = attachmentLibraryService.searchResources(workspaceId, query, fileTypes, pageable);
        Page<AttachmentLibraryResponse> responseList = attachmentLibraryDtoList
                .map(AttachmentLibraryResponse::fromDto);
        log.info("[GET] Retrieved {} Attachment Libraries matching query: {}", responseList.getSize(), query);
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Get Attachment Libraries By Id")
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentLibraryResponse> getAttachmentLibraryById(@PathVariable Long tenantId,
                                                                              @PathVariable String id) {
        log.info("[GET] Received request to get Attachment Libraries with ID: {}", id);
        AttachmentLibraryDto attachmentLibraryDto = attachmentLibraryService.getResourceById(id);
        AttachmentLibraryResponse response = AttachmentLibraryResponse.fromDto(attachmentLibraryDto);
        log.info("[GET] Retrieved Attachment Libraries with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Attachment Library")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentLibraryResponse> createAttachmentLibrary(@PathVariable Long tenantId,
                                                                             @PathVariable String workspaceId,
                                                                             @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                                                                             @RequestParam("file") MultipartFile file) {
        log.info("[POST] Received request to create Attachment Library with file name: {}", file.getOriginalFilename());
        AttachmentLibraryDto attachmentLibraryDto = attachmentLibraryService.createResource(workspaceId, file);
        AttachmentLibraryResponse response = AttachmentLibraryResponse.fromDto(attachmentLibraryDto);
        log.info("[POST] Created Attachment Library with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete Attachment Library")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachmentLibrary(@PathVariable Long tenantId, @PathVariable String id) {
        log.info("[DELETE] Received request to delete Attachment Library with ID: {}", id);
        attachmentLibraryService.deleteResource(id);
        log.info("[DELETE] Deleted Attachment Library with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rename Attachment Library File By Id")
    @PutMapping("/{id}/rename")
    public ResponseEntity<AttachmentLibraryResponse> renameAttachmentLibraryById(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @RequestBody AttachmentFileRenameRequest attachmentFileRenameRequest) {
        log.info("[PUT] Received request to rename Attachment Library with ID: {}", id);
        AttachmentLibraryDto attachmentLibraryDto = attachmentLibraryService.renameAttachmentLibraryById(id, attachmentFileRenameRequest.getFileName());
        AttachmentLibraryResponse response = AttachmentLibraryResponse.fromDto(attachmentLibraryDto);
        log.info("[PUT] Renamed Attachment Library with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace Attachment Library File By Id")
    @PutMapping("/{id}/replace")
    public ResponseEntity<AttachmentLibraryResponse> replaceAttachmentLibraryFile(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @Parameter(
                    description = "File to upload",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
           ) {
        log.info("[PUT] Received request to replace Attachment Library with ID: {}", id);
        AttachmentLibraryDto attachmentLibraryDto = attachmentLibraryService.replaceAttachmentLibraryFile(id, file);
        AttachmentLibraryResponse response = AttachmentLibraryResponse.fromDto(attachmentLibraryDto);
        log.info("[PUT] Replaced Attachment Library with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}
