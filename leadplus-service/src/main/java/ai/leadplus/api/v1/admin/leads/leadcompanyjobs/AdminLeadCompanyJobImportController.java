package ai.leadplus.api.v1.admin.leads.leadcompanyjobs;

import ai.leadplus.application.leadcompanyjob.LeadCompanyJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/admin/company-jobs")
@RequiredArgsConstructor
@Tag(name = "Company Jobs", description = "Company Jobs APIs")
public class AdminLeadCompanyJobImportController {

    private final LeadCompanyJobService leadCompanyJobService;

    @Operation(summary = "Import Jobs from JSON File", description = "Accepts a JSON file containing a list of company job payloads")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importJobs(
            @Parameter(description = "JSON file containing list of company job payloads", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        log.info("[POST] Import jobs from file: {}", file.getOriginalFilename());
        leadCompanyJobService.processJobsFile(file);
        log.info("[POST] Import complete for file: {}", file.getOriginalFilename());
        return ResponseEntity.noContent().build();
    }
}
