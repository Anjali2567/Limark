package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leadcompanyjob.LeadCompanyJobDto;
import ai.leadplus.application.leadcompanyjob.LeadCompanyJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/leads/companies/{companyIdOrDomain}/jobs")
@RequiredArgsConstructor
@Tag(name = "Company Jobs", description = "Company Jobs APIs")
public class LeadCompanyJobController {

    private final LeadCompanyJobService leadCompanyJobService;

    @Operation(summary = "Get All Jobs for Company")
    @GetMapping
    public ResponseEntity<Page<LeadCompanyJobResponse>> getJobs(@PathVariable String companyIdOrDomain, Pageable pageable) {
        log.info("[GET] Get jobs for companyIdOrDomain {}", companyIdOrDomain);
        Page<LeadCompanyJobResponse> responses = leadCompanyJobService.getJobsByCompanyIdOrDomain(companyIdOrDomain, pageable)
                .map(LeadCompanyJobResponse::fromDto);
        log.info("[GET] Returning {} jobs for companyIdOrDomain {}", responses.getTotalElements(), companyIdOrDomain);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Job by ID")
    @GetMapping("/{jobId}")
    public ResponseEntity<LeadCompanyJobResponse> getJobById(@PathVariable String companyIdOrDomain,
                                                             @PathVariable String jobId) {
        log.info("[GET] Get jobId {} for companyIdOrDomain {}", jobId, companyIdOrDomain);
        LeadCompanyJobDto dto = leadCompanyJobService.getJobById(companyIdOrDomain, jobId);
        LeadCompanyJobResponse response = LeadCompanyJobResponse.fromDto(dto);
        log.info("[GET] Returning jobId {} for companyIdOrDomain {}", jobId, companyIdOrDomain);
        return ResponseEntity.ok(response);
    }
}
