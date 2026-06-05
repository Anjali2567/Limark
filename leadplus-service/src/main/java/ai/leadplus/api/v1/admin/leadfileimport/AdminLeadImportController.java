package ai.leadplus.api.v1.admin.leadfileimport;

import ai.leadplus.application.leadfileimport.ImportPreviewResult;
import ai.leadplus.application.leadfileimport.LeadFileImportDto;
import ai.leadplus.application.leadfileimport.LeadFileImportRecordDto;
import ai.leadplus.application.leadfileimport.LeadFileImportService;
import ai.leadplus.application.leadfileimport.LeadFileImportUploadResult;
import ai.leadplus.domain.leadfileimport.TargetEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/admin/lead-imports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Lead Imports", description = "Excel-based lead import APIs")
public class AdminLeadImportController {

    private final LeadFileImportService importService;

    @Operation(
            summary = "Upload a Contact import file",
            description = "Required column: CONTACT_EMAIL. System auto-maps all recognised columns. " +
                    "Returns batch details, detected/ignored columns, and template guide."
    )
    @PostMapping(value = "/contacts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadContacts(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        log.info("[POST] Received request to upload Contact import file={} user={}", file.getOriginalFilename(), auth.getName());
        LeadFileImportUploadResult result = importService.upload(file, TargetEntity.LEAD_CONTACT, auth.getName());
        log.info("[POST] Uploaded Contact import batchId={} detectedColumns={}", result.getBatch().getId(), result.getDetectedColumns().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponse.from(result));
    }

    @Operation(
            summary = "Upload a Company import file",
            description = "Required column: COMPANY_DOMAIN. System auto-maps all recognised columns. " +
                    "Returns batch details, detected/ignored columns, and template guide."
    )
    @PostMapping(value = "/companies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadCompanies(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        log.info("[POST] Received request to upload Company import file={} user={}", file.getOriginalFilename(), auth.getName());
        LeadFileImportUploadResult result = importService.upload(file, TargetEntity.LEAD_COMPANY, auth.getName());
        log.info("[POST] Uploaded Company import batchId={} detectedColumns={}", result.getBatch().getId(), result.getDetectedColumns().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponse.from(result));
    }

    @Operation(
            summary = "Preview the import (dry-run)",
            description = "Returns three lists: toInsert, toUpdate, toSkip — with full row details."
    )
    @PostMapping("/{id}/preview")
    public ResponseEntity<PreviewResponse> preview(@PathVariable Long id) {
        log.info("[POST] Received request to preview importId={}", id);
        ImportPreviewResult result = importService.preview(id);
        log.info("[POST] Preview completed for importId={} toInsert={} toUpdate={} toSkip={}", id, result.getInsertCount(), result.getUpdateCount(), result.getSkipCount());
        return ResponseEntity.ok(PreviewResponse.from(result));
    }

    @Operation(summary = "Confirm and execute the import batch")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BatchResponse> confirm(@PathVariable Long id) {
        log.info("[POST] Received request to confirm importId={}", id);
        LeadFileImportDto dto = importService.confirm(id);
        log.info("[POST] Confirmed importId={} status={} inserted={} updated={} failed={}", id, dto.getStatus(), dto.getInsertedRecords(), dto.getUpdatedRecords(), dto.getFailedRecords());
        return ResponseEntity.ok(BatchResponse.from(dto));
    }

    @Operation(summary = "Rollback a completed import batch")
    @PostMapping("/{id}/rollback")
    public ResponseEntity<BatchResponse> rollback(@PathVariable Long id) {
        log.info("[POST] Received request to rollback importId={}", id);
        LeadFileImportDto dto = importService.rollback(id);
        log.info("[POST] Rolled back importId={} status={}", id, dto.getStatus());
        return ResponseEntity.ok(BatchResponse.from(dto));
    }

    @Operation(summary = "List all import batches (newest first)")
    @GetMapping
    public ResponseEntity<Page<BatchResponse>> listBatches(
            @RequestParam(required = false) TargetEntity targetEntity,
            Pageable pageable) {
        log.info("[GET] Received request to list import batches targetEntity={}", targetEntity);
        Page<LeadFileImportDto> page = importService.listBatches(targetEntity, pageable);
        log.info("[GET] Retrieved {} import batches", page.getTotalElements());
        return ResponseEntity.ok(page.map(BatchResponse::from));
    }

    @Operation(summary = "View per-row records for a batch")
    @GetMapping("/{id}/records")
    public ResponseEntity<Page<RecordResponse>> getRecords(
            @PathVariable Long id, Pageable pageable) {
        log.info("[GET] Received request to get records for importId={}", id);
        Page<LeadFileImportRecordDto> page = importService.getRecords(id, pageable);
        log.info("[GET] Retrieved {} records for importId={}", page.getTotalElements(), id);
        return ResponseEntity.ok(page.map(RecordResponse::from));
    }
}