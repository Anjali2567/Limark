package ai.leadplus.application.leadcompanyjob;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.scrapejob.JobDetailPayload;
import ai.leadplus.domain.leadcompanyjobs.LeadCompanyJob;
import ai.leadplus.domain.leadcompanyjobs.LeadCompanyJobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadCompanyJobService {

    private final LeadCompanyJobRepository leadCompanyJobRepository;
    private final LeadCompanyService leadCompanyService;
    private final ObjectMapper objectMapper;

    public Page<LeadCompanyJobDto> getJobsByCompanyIdOrDomain(String companyIdOrDomain, Pageable pageable) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        return leadCompanyJobRepository.findAllByLeadCompanyIdAndActiveTrue(leadCompanyDto.getId(), pageable)
                .map(LeadCompanyJobDto::fromEntity);
    }

    public LeadCompanyJobDto getJobById(String companyIdOrDomain, String jobId) {
        LeadCompanyDto leadCompanyDto = getLeadCompany(companyIdOrDomain);
        return LeadCompanyJobDto.fromEntity(findJobByCompanyIdAndId(jobId, leadCompanyDto.getId()));
    }

    @Transactional
    public void replaceJobsForCompany(Long leadCompanyId, List<LeadCompanyJobDto> jobs) {
        List<LeadCompanyJob> existing = leadCompanyJobRepository.findAllByLeadCompanyIdAndActiveTrue(leadCompanyId);
        if (!CollectionUtils.isEmpty(existing)) {
            existing.forEach(job -> job.setActive(false));
            leadCompanyJobRepository.saveAll(existing);
            log.info("Deleted {} existing jobs for leadCompanyId {}", existing.size(), leadCompanyId);
        }

        List<LeadCompanyJob> newJobs = jobs.stream()
                .map(dto -> {
                    dto.setLeadCompanyId(leadCompanyId);
                    dto.setActive(true);
                    return dto.toEntity();
                })
                .toList();
        leadCompanyJobRepository.saveAll(newJobs);
        log.info("Saved {} new jobs for leadCompanyId {}", newJobs.size(), leadCompanyId);
    }

    public void processJobsFile(MultipartFile file) {
        List<LeadCompanyJobFilePayload> payloads;
        try {
            payloads = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse jobs JSON file: " + e.getMessage(), e);
        }

        for (LeadCompanyJobFilePayload payload : payloads) {
            String url = payload.getUrl();
            try {
                String domain = extractDomain(url);
                LeadCompanyDto company = leadCompanyService.getCompanyByIdOrDomain(domain);

                List<LeadCompanyJobDto> jobs = payload.getData().getCareers().stream()
                        .flatMap(careers -> mapJobEntriesToDtos(careers.getJobs()).stream())
                        .toList();

                replaceJobsForCompany(company.getId(), jobs);
                log.info("Processed {} jobs for domain {} (companyId {})", jobs.size(), domain, company.getId());
            } catch (Exception e) {
                log.warn("Failed to process jobs for URL {}: {}", url, e.getMessage());
            }
        }
    }

    public List<LeadCompanyJob> findJobsNeedingDetailScrape(List<Long> busyJobIds, int limit) {
        return leadCompanyJobRepository.findByActiveTrueAndJobUrlIsNotNullAndIdNotIn(busyJobIds, PageRequest.of(0, limit));
    }

    public List<LeadCompanyJob> findJobsNeedingDetailScrapeForCompany(Long companyId, List<Long> busyJobIds) {
        return leadCompanyJobRepository.findByLeadCompanyIdAndActiveTrueAndJobUrlIsNotNullAndIdNotIn(companyId, busyJobIds);
    }

    public void enrichJobWithDetails(Long jobId, JobDetailPayload payload) {
        LeadCompanyJob job = findJobById(jobId);
        job.setRequirements(payload.getRequirements());
        job.setTechnologies(payload.getTechnologies());
        job.setTools(payload.getTools());
        job.setServices(payload.getServices());
        leadCompanyJobRepository.save(job);
    }

    public void aggregateTechToCompany(Long companyId) {
        List<LeadCompanyJob> jobs = leadCompanyJobRepository.findAllByLeadCompanyIdAndActiveTrue(companyId);

        List<String> allTech = new ArrayList<>();
        List<String> allTools = new ArrayList<>();
        List<String> allServices = new ArrayList<>();

        jobs.forEach(j -> {
            if (!CollectionUtils.isEmpty(j.getTechnologies())) allTech.addAll(j.getTechnologies());
            if (!CollectionUtils.isEmpty(j.getTools())) allTools.addAll(j.getTools());
            if (!CollectionUtils.isEmpty(j.getServices())) allServices.addAll(j.getServices());
        });

        leadCompanyService.updateScrapedTech(companyId,
                allTech.stream().distinct().sorted().toList(),
                allTools.stream().distinct().sorted().toList(),
                allServices.stream().distinct().sorted().toList());
    }

    private String extractDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) {
                throw new IllegalArgumentException("Cannot extract host from URL: " + url);
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public List<LeadCompanyJobDto> mapJobEntriesToDtos(List<LeadCompanyJobFilePayload.JobEntry> entries) {
        if (CollectionUtils.isEmpty(entries)) {
            return List.of();
        }
        return entries.stream()
                .map(entry -> {
                    LocalDateTime postedDate = null;
                    if (StringUtils.hasText(entry.getPostedDate())) {
                        try {
                            postedDate = LocalDateTime.parse(entry.getPostedDate());
                        } catch (Exception e) {
                            log.warn("Could not parse postedDate '{}': {}", entry.getPostedDate(), e.getMessage());
                        }
                    }
                    return LeadCompanyJobDto.builder()
                            .type(entry.getType())
                            .title(entry.getTitle())
                            .skills(entry.getSkills())
                            .jobUrl(entry.getJobUrl())
                            .applyUrl(entry.getApplyUrl())
                            .benefits(entry.getBenefits())
                            .location(entry.getLocation())
                            .department(entry.getDepartment())
                            .postedDate(postedDate)
                            .description(entry.getDescription())
                            .requirements(entry.getRequirements())
                            .active(true)
                            .build();
                })
                .toList();
    }

    private LeadCompanyJob findJobById(Long jobId) {
        return leadCompanyJobRepository.findByIdAndActiveTrue(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
    }

    private LeadCompanyJob findJobByCompanyIdAndId(String jobId, Long companyId) {
        return leadCompanyJobRepository.findByIdAndLeadCompanyIdAndActiveTrue(Long.parseLong(jobId), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId + " for companyId: " + companyId));
    }

    private LeadCompanyDto getLeadCompany(String companyIdOrDomain) {
        return leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
    }

}
