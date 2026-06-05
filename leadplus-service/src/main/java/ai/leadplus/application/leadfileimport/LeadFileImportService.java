package ai.leadplus.application.leadfileimport;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ImportRowException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import ai.leadplus.domain.leadcontacts.LeadContact;
import ai.leadplus.domain.leadcontacts.LeadContactRepository;
import ai.leadplus.domain.leadfileimport.*;
import ai.leadplus.infrastructure.aws.s3.AwsS3Client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadFileImportService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final LeadFileImportRepository importRepository;
    private final LeadFileImportRecordRepository recordRepository;
    private final LeadCompanyRepository companyRepository;
    private final LeadContactRepository contactRepository;
    private final AwsS3Service awsS3Service;
    private final AwsS3Client awsS3Client;
    private final ObjectMapper objectMapper;

    public LeadFileImportUploadResult upload(MultipartFile file,
                                             TargetEntity targetEntity,
                                             String uploadedByUserId) {
        validateFileType(file);

        List<String> fileHeaders = ExcelParserUtil.extractHeaders(file);
        if (fileHeaders.isEmpty()) {
            throw new BadRequestException("The uploaded file has no header row.");
        }

        Map<String, String> templateMap = targetEntity == TargetEntity.LEAD_CONTACT
                ? ImportColumnMapping.CONTACT_COLUMN_MAP
                : ImportColumnMapping.COMPANY_COLUMN_MAP;

        List<String> requiredColumns = targetEntity == TargetEntity.LEAD_CONTACT
                ? List.of("CONTACT_EMAIL", "COMPANY_DOMAIN")
                : List.of("COMPANY_DOMAIN");

        Map<String, String> appliedMapping = new LinkedHashMap<>();
        List<String> detectedColumns = new ArrayList<>();
        List<String> ignoredColumns = new ArrayList<>();

        for (String header : fileHeaders) {
            String fieldName = templateMap.get(header);
            if (fieldName != null) {
                appliedMapping.put(header, fieldName);
                detectedColumns.add(header);
            } else {
                ignoredColumns.add(header);
            }
        }

        List<String> missingCols = requiredColumns.stream()
                .filter(col -> !fileHeaders.contains(col))
                .toList();
        String missingRequired = missingCols.isEmpty() ? null
                : String.join(", ", missingCols) + (missingCols.size() == 1 ? " is" : " are")
                + " required but not found in the uploaded file.";
        if (missingRequired != null) {
            log.warn("[LeadImport] Missing required columns={} file={}", missingCols, file.getOriginalFilename());
        }

        LeadFileImport batch = LeadFileImport.builder()
                .targetEntity(targetEntity)
                .importTypes(new ArrayList<>())
                .originalFileName(file.getOriginalFilename())
                .mappedFieldsJson(toJson(appliedMapping))
                .status(ImportStatus.PENDING)
                .uploadedByUserId(uploadedByUserId)
                .build();
        batch = importRepository.save(batch);

        String s3Key = awsS3Service.uploadFile(file, "lead-imports", String.valueOf(batch.getId()));
        batch.setFileStorageKey(s3Key);
        batch = importRepository.save(batch);

        log.info("[LeadImport] Uploaded batch id={} targetEntity={} detectedCols={} ignoredCols={}",
                batch.getId(), targetEntity, detectedColumns.size(), ignoredColumns.size());

        return LeadFileImportUploadResult.builder()
                .batch(LeadFileImportDto.fromEntity(batch))
                .detectedColumns(detectedColumns)
                .ignoredColumns(ignoredColumns)
                .missingRequiredColumn(missingRequired)
                .templateGuide(targetEntity == TargetEntity.LEAD_CONTACT
                        ? ImportColumnMapping.CONTACT_TEMPLATE_GUIDE
                        : ImportColumnMapping.COMPANY_TEMPLATE_GUIDE)
                .appliedMapping(appliedMapping)
                .build();
    }

    public ImportPreviewResult preview(Long importId) {
        LeadFileImport batch = findBatch(importId);
        assertStatus(batch, ImportStatus.PENDING);

        Map<String, String> mapping = parseMapping(batch.getMappedFieldsJson());
        List<Map<String, String>> rows = readRowsFromS3(batch);

        List<ImportPreviewResult.InsertPreviewItem> toInsert = new ArrayList<>();
        List<ImportPreviewResult.UpdatePreviewItem> toUpdate = new ArrayList<>();
        List<ImportPreviewResult.SkipPreviewItem> toSkip = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> normalized = applyMapping(rows.get(i), mapping);
            evaluateRow(i + 1, normalized, batch.getTargetEntity(), toInsert, toUpdate, toSkip);
        }

        return ImportPreviewResult.builder()
                .totalRows(rows.size())
                .insertCount(toInsert.size())
                .updateCount(toUpdate.size())
                .skipCount(toSkip.size())
                .toInsert(toInsert)
                .toUpdate(toUpdate)
                .toSkip(toSkip)
                .build();
    }

    public LeadFileImportDto confirm(Long importId) {
        LeadFileImport batch = findBatch(importId);
        assertStatus(batch, ImportStatus.PENDING);

        Map<String, String> mapping = parseMapping(batch.getMappedFieldsJson());
        List<Map<String, String>> rows = readRowsFromS3(batch);

        batch.setStatus(ImportStatus.IN_PROGRESS);
        batch.setStartedAt(LocalDateTime.now());
        batch.setTotalRecords(rows.size());
        importRepository.save(batch);

        int inserted = 0, updated = 0, skipped = 0, failed = 0;
        Set<ImportType> encounteredTypes = new LinkedHashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> raw = rows.get(i);
            Map<String, String> normalized = applyMapping(raw, mapping);

            LeadFileImportRecord record = LeadFileImportRecord.builder()
                    .importId(batch.getId())
                    .recordNumber(i + 1)
                    .rawPayloadJson(toJson(raw))
                    .normalizedPayloadJson(toJson(normalized))
                    .build();

            try {
                ProcessResult result = processRow(normalized, batch.getTargetEntity());

                record.setMatchKey(result.matchKey());
                record.setResolvedImportType(result.importType());
                record.setStatus(result.status());
                record.setTargetEntityId(result.entityId());
                record.setChangesJson(toJson(result.changes()));
                record.setProcessedAt(LocalDateTime.now());

                encounteredTypes.add(result.importType());

                switch (result.status()) {
                    case INSERTED -> inserted++;
                    case UPDATED -> updated++;
                    case SKIPPED, NO_CHANGE -> skipped++;
                    default -> {
                    }
                }

            } catch (ImportRowException e) {
                record.setStatus(RecordStatus.FAILED);
                record.setErrorCode(e.getErrorCode());
                record.setErrorMessage(e.getMessage());
                record.setProcessedAt(LocalDateTime.now());
                failed++;
            } catch (Exception e) {
                record.setStatus(RecordStatus.FAILED);
                record.setErrorCode("UNEXPECTED_ERROR");
                record.setErrorMessage(e.getMessage());
                record.setProcessedAt(LocalDateTime.now());
                failed++;
                log.warn("[LeadImport] Unexpected error row={} importId={}: {}",
                        i + 1, importId, e.getMessage());
            }

            recordRepository.save(record);
        }

        batch.setImportTypes(new ArrayList<>(encounteredTypes));
        batch.setProcessedRecords(rows.size());
        batch.setInsertedRecords(inserted);
        batch.setUpdatedRecords(updated);
        batch.setSkippedRecords(skipped);
        batch.setFailedRecords(failed);
        batch.setCompletedAt(LocalDateTime.now());
        batch.setStatus(failed == 0 ? ImportStatus.COMPLETED
                : (inserted + updated > 0 ? ImportStatus.PARTIAL_SUCCESS : ImportStatus.FAILED));

        log.info("[LeadImport] Confirmed importId={} types={} inserted={} updated={} skipped={} failed={}",
                importId, encounteredTypes, inserted, updated, skipped, failed);
        return LeadFileImportDto.fromEntity(importRepository.save(batch));
    }

    public LeadFileImportDto rollback(Long importId) {
        LeadFileImport batch = findBatch(importId);
        if (batch.getStatus() != ImportStatus.COMPLETED
                && batch.getStatus() != ImportStatus.PARTIAL_SUCCESS) {
            throw new BadRequestException(
                    "Only COMPLETED or PARTIAL_SUCCESS batches can be rolled back. Current: "
                            + batch.getStatus());
        }

        List<LeadFileImportRecord> records = recordRepository.findAllByImportId(importId);
        for (LeadFileImportRecord record : records) {
            try {
                if (record.getStatus() == RecordStatus.INSERTED) {
                    rollbackInsert(record, batch.getTargetEntity());
                } else if (record.getStatus() == RecordStatus.UPDATED) {
                    rollbackUpdate(record, batch.getTargetEntity());
                }
            } catch (Exception e) {
                log.warn("[LeadImport] Rollback error recordId={}: {}", record.getId(), e.getMessage());
            }
        }

        batch.setStatus(ImportStatus.ROLLED_BACK);
        log.info("[LeadImport] Rolled back importId={}", importId);
        return LeadFileImportDto.fromEntity(importRepository.save(batch));
    }

    public Page<LeadFileImportDto> listBatches(TargetEntity targetEntity, Pageable pageable) {
        if (targetEntity != null) {
            return importRepository.findAllByTargetEntityOrderByCreatedAtDesc(targetEntity, pageable)
                    .map(LeadFileImportDto::fromEntity);
        }
        return importRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(LeadFileImportDto::fromEntity);
    }

    public Page<LeadFileImportRecordDto> getRecords(Long importId, Pageable pageable) {
        findBatch(importId);
        return recordRepository.findAllByImportId(importId, pageable)
                .map(LeadFileImportRecordDto::fromEntity);
    }

    private ProcessResult processRow(Map<String, String> normalized, TargetEntity target) {
        return target == TargetEntity.LEAD_CONTACT
                ? processContactRow(normalized)
                : processCompanyRow(normalized);
    }

    private ProcessResult processContactRow(Map<String, String> row) {
        String email = row.get("email");
        validateEmail(email);

        String companyDomain = row.get("companyDomain");
        validateDomain(companyDomain);
        LeadCompany company = companyRepository.findByDomainAndActiveTrue(companyDomain.trim())
                .orElseThrow(() -> new ImportRowException("COMPANY_NOT_FOUND",
                        "Company not found for domain: " + companyDomain));

        Optional<LeadContact> existing = contactRepository.findByEmailAndActiveTrue(email);

        if (existing.isPresent()) {
            LeadContact contact = existing.get();
            List<FieldChange> changes = enrichContact(contact, row);
            if (contact.getLeadCompanyId() == null) {
                FieldChange.trackIfChanged(changes, "leadCompanyId", null, String.valueOf(company.getId()));
                contact.setLeadCompanyId(company.getId());
            }
            if (changes.isEmpty()) {
                return new ProcessResult(email, ImportType.CONTACT_ENRICHMENT,
                        RecordStatus.NO_CHANGE, contact.getId(), List.of());
            }
            contactRepository.save(contact);
            return new ProcessResult(email, ImportType.CONTACT_ENRICHMENT,
                    RecordStatus.UPDATED, contact.getId(), changes);
        } else {
            LeadContact contact = buildNewContact(row);
            contact.setLeadCompanyId(company.getId());
            LeadContact saved = contactRepository.save(contact);
            return new ProcessResult(email, ImportType.CONTACT_CREATE,
                    RecordStatus.INSERTED, saved.getId(), List.of());
        }
    }

    private ProcessResult processCompanyRow(Map<String, String> row) {
        String domain = row.get("domain");
        validateDomain(domain);

        Optional<LeadCompany> existing = companyRepository.findByDomainAndActiveTrue(domain);

        if (existing.isPresent()) {
            List<FieldChange> changes = enrichCompany(existing.get(), row);
            if (changes.isEmpty()) {
                return new ProcessResult(domain, ImportType.COMPANY_ENRICHMENT,
                        RecordStatus.NO_CHANGE, existing.get().getId(), List.of());
            }
            companyRepository.save(existing.get());
            return new ProcessResult(domain, ImportType.COMPANY_ENRICHMENT,
                    RecordStatus.UPDATED, existing.get().getId(), changes);
        } else {
            LeadCompany saved = companyRepository.save(buildNewCompany(row));
            return new ProcessResult(domain, ImportType.COMPANY_CREATE,
                    RecordStatus.INSERTED, saved.getId(), List.of());
        }
    }

    private List<FieldChange> enrichContact(LeadContact c, Map<String, String> row) {
        List<FieldChange> ch = FieldChange.newList();
        fillIfEmpty(ch, "firstName", c.getFirstName(), row, c::setFirstName);
        fillIfEmpty(ch, "lastName", c.getLastName(), row, c::setLastName);
        fillIfEmpty(ch, "title", c.getTitle(), row, c::setTitle);
        fillIfEmpty(ch, "seniority", c.getSeniority(), row, c::setSeniority);
        fillIfEmpty(ch, "department", c.getDepartment(), row, c::setDepartment);
        fillIfEmpty(ch, "phoneE164", c.getPhoneE164(), row, c::setPhoneE164);
        fillIfEmpty(ch, "linkedinUrl", c.getLinkedinUrl(), row, c::setLinkedinUrl);
        fillIfEmpty(ch, "locationCity", c.getLocationCity(), row, c::setLocationCity);
        fillIfEmpty(ch, "locationState", c.getLocationState(), row, c::setLocationState);
        fillIfEmpty(ch, "locationCountry", c.getLocationCountry(), row, c::setLocationCountry);
        fillIfEmpty(ch, "locationZip", c.getLocationZip(), row, c::setLocationZip);
        fillIfEmpty(ch, "personaMatch", c.getPersonaMatch(), row, c::setPersonaMatch);
        fillIfEmpty(ch, "notes", c.getNotes(), row, c::setNotes);
        return ch;
    }

    private List<FieldChange> enrichCompany(LeadCompany c, Map<String, String> row) {
        List<FieldChange> ch = FieldChange.newList();
        fillIfEmpty(ch, "name", c.getName(), row, c::setName);
        fillIfEmpty(ch, "websiteUrl", c.getWebsiteUrl(), row, c::setWebsiteUrl);
        fillIfEmpty(ch, "linkedinUrl", c.getLinkedinUrl(), row, c::setLinkedinUrl);
        fillIfEmpty(ch, "twitterUrl", c.getTwitterUrl(), row, c::setTwitterUrl);
        fillIfEmpty(ch, "facebookUrl", c.getFacebookUrl(), row, c::setFacebookUrl);
        fillIfEmpty(ch, "phoneNumber", c.getPhoneNumber(), row, c::setPhoneNumber);
        fillIfEmpty(ch, "industry", c.getIndustry(), row, c::setIndustry);
        fillIfEmpty(ch, "employeeCount", c.getEmployeeCount(), row, c::setEmployeeCount);
        fillIfEmpty(ch, "hqCity", c.getHqCity(), row, c::setHqCity);
        fillIfEmpty(ch, "hqState", c.getHqState(), row, c::setHqState);
        fillIfEmpty(ch, "hqCountry", c.getHqCountry(), row, c::setHqCountry);
        fillIfEmpty(ch, "postalCode", c.getPostalCode(), row, c::setPostalCode);
        fillIfEmpty(ch, "territory", c.getTerritory(), row, c::setTerritory);
        fillIfEmpty(ch, "region", c.getRegion(), row, c::setRegion);
        fillIfEmpty(ch, "icpTag", c.getIcpTag(), row, c::setIcpTag);
        mergeSegments(ch, c, row);
        fillIfEmpty(ch, "accountSummary", c.getAccountSummary(), row, c::setAccountSummary);
        return ch;
    }

    private void mergeSegments(List<FieldChange> changes, LeadCompany c, Map<String, String> row) {
        String incoming = row.get("segments");
        if (!StringUtils.hasText(incoming)) return;
        List<String> toAdd = Arrays.stream(incoming.split(","))
                .map(String::trim).filter(StringUtils::hasText).toList();
        List<String> existing = c.getSegments() != null ? new ArrayList<>(c.getSegments()) : new ArrayList<>();
        List<String> added = toAdd.stream().filter(s -> !existing.contains(s)).toList();
        if (!added.isEmpty()) {
            String prev = existing.isEmpty() ? null : String.join(",", existing);
            existing.addAll(added);
            FieldChange.trackIfChanged(changes, "segments", prev, String.join(",", existing));
            c.setSegments(existing);
        }
    }

    private void fillIfEmpty(List<FieldChange> changes,
                             String field,
                             String currentValue,
                             Map<String, String> row,
                             Consumer<String> setter) {
        String newValue = row.get(field);
        if (!StringUtils.hasText(currentValue) && StringUtils.hasText(newValue)) {
            FieldChange.trackIfChanged(changes, field, currentValue, newValue);
            setter.accept(newValue);
        }
    }

    private void evaluateRow(int rowNum,
                             Map<String, String> normalized,
                             TargetEntity target,
                             List<ImportPreviewResult.InsertPreviewItem> toInsert,
                             List<ImportPreviewResult.UpdatePreviewItem> toUpdate,
                             List<ImportPreviewResult.SkipPreviewItem> toSkip) {
        try {
            if (target == TargetEntity.LEAD_CONTACT) {
                evaluateContactRow(rowNum, normalized, toInsert, toUpdate, toSkip);
            } else {
                evaluateCompanyRow(rowNum, normalized, toInsert, toUpdate, toSkip);
            }
        } catch (ImportRowException e) {
            toSkip.add(ImportPreviewResult.SkipPreviewItem.builder()
                    .rowNumber(rowNum)
                    .matchKey(normalized.get("email") != null
                            ? normalized.get("email") : normalized.get("domain"))
                    .reason(RecordStatus.FAILED)
                    .errorCode(e.getErrorCode())
                    .errorMessage(e.getMessage())
                    .normalizedPayload(normalized)
                    .build());
        }
    }

    private void evaluateContactRow(int rowNum,
                                    Map<String, String> normalized,
                                    List<ImportPreviewResult.InsertPreviewItem> toInsert,
                                    List<ImportPreviewResult.UpdatePreviewItem> toUpdate,
                                    List<ImportPreviewResult.SkipPreviewItem> toSkip) {
        String email = normalized.get("email");
        validateEmail(email);

        String companyDomain = normalized.get("companyDomain");
        validateDomain(companyDomain);
        LeadCompany company = companyRepository.findByDomainAndActiveTrue(companyDomain.trim())
                .orElseThrow(() -> new ImportRowException("COMPANY_NOT_FOUND",
                        "Company not found for domain: " + companyDomain));

        Optional<LeadContact> existing = contactRepository.findByEmailAndActiveTrue(email);

        if (existing.isEmpty()) {
            Map<String, String> fieldsToCreate = normalized.entrySet().stream()
                    .filter(e -> StringUtils.hasText(e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (a, b) -> a, LinkedHashMap::new));

            toInsert.add(ImportPreviewResult.InsertPreviewItem.builder()
                    .rowNumber(rowNum)
                    .matchKey(email)
                    .resolvedImportType(ImportType.CONTACT_CREATE)
                    .fieldsToCreate(fieldsToCreate)
                    .build());
        } else {
            List<FieldChange> changes = enrichContact(cloneContact(existing.get()), normalized);
            if (existing.get().getLeadCompanyId() == null) {
                FieldChange.trackIfChanged(changes, "leadCompanyId", null, String.valueOf(company.getId()));
            }

            if (changes.isEmpty()) {
                toSkip.add(ImportPreviewResult.SkipPreviewItem.builder()
                        .rowNumber(rowNum)
                        .matchKey(email)
                        .reason(RecordStatus.NO_CHANGE)
                        .errorMessage("All fields already populated for this contact.")
                        .normalizedPayload(normalized)
                        .build());
            } else {
                Map<String, String> newValues = changes.stream()
                        .collect(Collectors.toMap(FieldChange::getField, FieldChange::getNewValue,
                                (a, b) -> a, LinkedHashMap::new));

                toUpdate.add(ImportPreviewResult.UpdatePreviewItem.builder()
                        .rowNumber(rowNum)
                        .matchKey(email)
                        .existingEntityId(existing.get().getId())
                        .resolvedImportType(ImportType.CONTACT_ENRICHMENT)
                        .fieldsToFill(changes.stream().map(FieldChange::getField).toList())
                        .newValues(newValues)
                        .build());
            }
        }
    }

    private void evaluateCompanyRow(int rowNum,
                                    Map<String, String> normalized,
                                    List<ImportPreviewResult.InsertPreviewItem> toInsert,
                                    List<ImportPreviewResult.UpdatePreviewItem> toUpdate,
                                    List<ImportPreviewResult.SkipPreviewItem> toSkip) {
        String domain = normalized.get("domain");
        validateDomain(domain);

        Optional<LeadCompany> existing = companyRepository.findByDomainAndActiveTrue(domain);

        if (existing.isEmpty()) {
            Map<String, String> fieldsToCreate = normalized.entrySet().stream()
                    .filter(e -> StringUtils.hasText(e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (a, b) -> a, LinkedHashMap::new));

            toInsert.add(ImportPreviewResult.InsertPreviewItem.builder()
                    .rowNumber(rowNum)
                    .matchKey(domain)
                    .resolvedImportType(ImportType.COMPANY_CREATE)
                    .fieldsToCreate(fieldsToCreate)
                    .build());
        } else {
            List<FieldChange> changes = enrichCompany(cloneCompany(existing.get()), normalized);

            if (changes.isEmpty()) {
                toSkip.add(ImportPreviewResult.SkipPreviewItem.builder()
                        .rowNumber(rowNum)
                        .matchKey(domain)
                        .reason(RecordStatus.NO_CHANGE)
                        .errorMessage("All fields already populated for this company.")
                        .normalizedPayload(normalized)
                        .build());
            } else {
                Map<String, String> newValues = changes.stream()
                        .collect(Collectors.toMap(FieldChange::getField, FieldChange::getNewValue,
                                (a, b) -> a, LinkedHashMap::new));

                toUpdate.add(ImportPreviewResult.UpdatePreviewItem.builder()
                        .rowNumber(rowNum)
                        .matchKey(domain)
                        .existingEntityId(existing.get().getId())
                        .resolvedImportType(ImportType.COMPANY_ENRICHMENT)
                        .fieldsToFill(changes.stream().map(FieldChange::getField).toList())
                        .newValues(newValues)
                        .build());
            }
        }
    }

    private void rollbackInsert(LeadFileImportRecord record, TargetEntity target) {
        if (record.getTargetEntityId() == null) return;
        if (target == TargetEntity.LEAD_CONTACT) {
            contactRepository.findByIdAndActiveTrue(record.getTargetEntityId())
                    .ifPresent(c -> {
                        c.setActive(false);
                        contactRepository.save(c);
                    });
        } else {
            companyRepository.findById(record.getTargetEntityId())
                    .ifPresent(c -> {
                        c.setActive(false);
                        companyRepository.save(c);
                    });
        }
    }

    private void rollbackUpdate(LeadFileImportRecord record, TargetEntity target) {
        if (record.getTargetEntityId() == null || record.getChangesJson() == null) return;
        Map<String, String> prev = new HashMap<>();
        parseChanges(record.getChangesJson()).forEach(fc -> prev.put(fc.getField(), fc.getPreviousValue()));

        if (target == TargetEntity.LEAD_CONTACT) {
            contactRepository.findByIdAndActiveTrue(record.getTargetEntityId())
                    .ifPresent(c -> {
                        applyContactRollback(c, prev);
                        contactRepository.save(c);
                    });
        } else {
            companyRepository.findById(record.getTargetEntityId())
                    .ifPresent(c -> {
                        applyCompanyRollback(c, prev);
                        companyRepository.save(c);
                    });
        }
    }

    private void applyContactRollback(LeadContact c, Map<String, String> prev) {
        if (prev.containsKey("firstName")) c.setFirstName(prev.get("firstName"));
        if (prev.containsKey("lastName")) c.setLastName(prev.get("lastName"));
        if (prev.containsKey("title")) c.setTitle(prev.get("title"));
        if (prev.containsKey("seniority")) c.setSeniority(prev.get("seniority"));
        if (prev.containsKey("department")) c.setDepartment(prev.get("department"));
        if (prev.containsKey("phoneE164")) c.setPhoneE164(prev.get("phoneE164"));
        if (prev.containsKey("linkedinUrl")) c.setLinkedinUrl(prev.get("linkedinUrl"));
        if (prev.containsKey("locationCity")) c.setLocationCity(prev.get("locationCity"));
        if (prev.containsKey("locationState")) c.setLocationState(prev.get("locationState"));
        if (prev.containsKey("locationCountry")) c.setLocationCountry(prev.get("locationCountry"));
        if (prev.containsKey("locationZip")) c.setLocationZip(prev.get("locationZip"));
        if (prev.containsKey("personaMatch")) c.setPersonaMatch(prev.get("personaMatch"));
        if (prev.containsKey("notes")) c.setNotes(prev.get("notes"));
        if (prev.containsKey("leadCompanyId")) {
            String val = prev.get("leadCompanyId");
            c.setLeadCompanyId(StringUtils.hasText(val) ? Long.valueOf(val) : null);
        }
    }

    private void applyCompanyRollback(LeadCompany c, Map<String, String> prev) {
        if (prev.containsKey("name")) c.setName(prev.get("name"));
        if (prev.containsKey("websiteUrl")) c.setWebsiteUrl(prev.get("websiteUrl"));
        if (prev.containsKey("linkedinUrl")) c.setLinkedinUrl(prev.get("linkedinUrl"));
        if (prev.containsKey("twitterUrl")) c.setTwitterUrl(prev.get("twitterUrl"));
        if (prev.containsKey("facebookUrl")) c.setFacebookUrl(prev.get("facebookUrl"));
        if (prev.containsKey("phoneNumber")) c.setPhoneNumber(prev.get("phoneNumber"));
        if (prev.containsKey("industry")) c.setIndustry(prev.get("industry"));
        if (prev.containsKey("employeeCount")) c.setEmployeeCount(prev.get("employeeCount"));
        if (prev.containsKey("hqCity")) c.setHqCity(prev.get("hqCity"));
        if (prev.containsKey("hqState")) c.setHqState(prev.get("hqState"));
        if (prev.containsKey("hqCountry")) c.setHqCountry(prev.get("hqCountry"));
        if (prev.containsKey("postalCode")) c.setPostalCode(prev.get("postalCode"));
        if (prev.containsKey("territory")) c.setTerritory(prev.get("territory"));
        if (prev.containsKey("region")) c.setRegion(prev.get("region"));
        if (prev.containsKey("icpTag")) c.setIcpTag(prev.get("icpTag"));
        if (prev.containsKey("segments")) {
            String val = prev.get("segments");
            c.setSegments(StringUtils.hasText(val)
                    ? new ArrayList<>(Arrays.asList(val.split(","))) : null);
        }
        if (prev.containsKey("accountSummary")) c.setAccountSummary(prev.get("accountSummary"));
    }

    private LeadContact buildNewContact(Map<String, String> row) {
        String first = row.get("firstName");
        String last = row.get("lastName");
        return LeadContact.builder()
                .email(row.get("email"))
                .firstName(first)
                .lastName(last)
                .fullName(buildFullName(first, last))
                .title(row.get("title"))
                .seniority(row.get("seniority"))
                .department(row.get("department"))
                .phoneE164(row.get("phoneE164"))
                .linkedinUrl(row.get("linkedinUrl"))
                .locationCity(row.get("locationCity"))
                .locationState(row.get("locationState"))
                .locationCountry(row.get("locationCountry"))
                .locationZip(row.get("locationZip"))
                .personaMatch(row.get("personaMatch"))
                .notes(row.get("notes"))
                .active(true)
                .build();
    }

    private LeadCompany buildNewCompany(Map<String, String> row) {
        return LeadCompany.builder()
                .domain(row.get("domain"))
                .name(row.get("name"))
                .websiteUrl(row.get("websiteUrl"))
                .linkedinUrl(row.get("linkedinUrl"))
                .twitterUrl(row.get("twitterUrl"))
                .facebookUrl(row.get("facebookUrl"))
                .phoneNumber(row.get("phoneNumber"))
                .industry(row.get("industry"))
                .employeeCount(row.get("employeeCount"))
                .hqCity(row.get("hqCity"))
                .hqState(row.get("hqState"))
                .hqCountry(row.get("hqCountry"))
                .postalCode(row.get("postalCode"))
                .territory(row.get("territory"))
                .region(row.get("region"))
                .icpTag(row.get("icpTag"))
                .segments(parseCommaSeparatedList(row.get("segments")))
                .accountSummary(row.get("accountSummary"))
                .active(true)
                .build();
    }

    private LeadContact cloneContact(LeadContact c) {
        return LeadContact.builder()
                .id(c.getId()).firstName(c.getFirstName()).lastName(c.getLastName())
                .title(c.getTitle()).seniority(c.getSeniority()).department(c.getDepartment())
                .phoneE164(c.getPhoneE164()).linkedinUrl(c.getLinkedinUrl())
                .locationCity(c.getLocationCity()).locationState(c.getLocationState())
                .locationCountry(c.getLocationCountry()).locationZip(c.getLocationZip())
                .personaMatch(c.getPersonaMatch()).notes(c.getNotes()).build();
    }

    private LeadCompany cloneCompany(LeadCompany c) {
        return LeadCompany.builder()
                .id(c.getId()).name(c.getName()).websiteUrl(c.getWebsiteUrl())
                .linkedinUrl(c.getLinkedinUrl()).twitterUrl(c.getTwitterUrl())
                .facebookUrl(c.getFacebookUrl()).phoneNumber(c.getPhoneNumber())
                .industry(c.getIndustry()).employeeCount(c.getEmployeeCount())
                .hqCity(c.getHqCity()).hqState(c.getHqState()).hqCountry(c.getHqCountry())
                .postalCode(c.getPostalCode()).territory(c.getTerritory()).region(c.getRegion())
                .icpTag(c.getIcpTag()).segments(c.getSegments() != null ? new ArrayList<>(c.getSegments()) : null).accountSummary(c.getAccountSummary())
                .build();
    }

    @SneakyThrows
    private List<Map<String, String>> readRowsFromS3(LeadFileImport batch) {
        S3Resource resource = awsS3Client.getObject(batch.getFileStorageKey());
        try (InputStream is = resource.getInputStream()) {
            return parseExcelStream(is, batch.getOriginalFileName());
        }
    }

    @SneakyThrows
    private List<Map<String, String>> parseExcelStream(InputStream is, String filename) {
        String name = filename == null ? "" : filename.toLowerCase();
        Workbook workbook = name.endsWith(".xlsx") ? new XSSFWorkbook(is) : new HSSFWorkbook(is);

        Sheet sheet = workbook.getSheetAt(0);
        List<Map<String, String>> rows = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            return rows;
        }

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) headers.add(getCellString(cell));

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Map<String, String> rowMap = new LinkedHashMap<>();
            boolean hasData = false;
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = cell == null ? null : getCellString(cell);
                rowMap.put(headers.get(j), value);
                if (StringUtils.hasText(value)) hasData = true;
            }
            if (hasData) rows.add(rowMap);
        }
        workbook.close();
        return rows;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Map<String, String> applyMapping(Map<String, String> rawRow,
                                             Map<String, String> mapping) {
        Map<String, String> normalized = new LinkedHashMap<>();
        mapping.forEach((col, field) -> {
            if (StringUtils.hasText(field)) normalized.put(field, rawRow.get(col));
        });
        return normalized;
    }

    private Map<String, String> parseMapping(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BadRequestException("Invalid mapping JSON: " + e.getMessage());
        }
    }

    private List<FieldChange> parseChanges(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void validateFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls")))
            throw new BadRequestException("Only .xlsx and .xls files are accepted.");
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email))
            throw new ImportRowException("MISSING_EMAIL", "Email is required.");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new ImportRowException("INVALID_EMAIL", "Invalid email format: " + email);
    }

    private void validateDomain(String domain) {
        if (!StringUtils.hasText(domain))
            throw new ImportRowException("MISSING_DOMAIN", "Domain is required.");
        if (!DOMAIN_PATTERN.matcher(domain.trim()).matches())
            throw new ImportRowException("INVALID_DOMAIN", "Invalid domain format: " + domain);
    }

    private LeadFileImport findBatch(Long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + id));
    }

    private void assertStatus(LeadFileImport batch, ImportStatus... allowed) {
        for (ImportStatus s : allowed) if (batch.getStatus() == s) return;
        throw new BadRequestException("Batch " + batch.getId() + " is in status "
                + batch.getStatus() + "; expected one of: " + Arrays.toString(allowed));
    }

    private static String buildFullName(String first, String last) {
        if (StringUtils.hasText(first) && StringUtils.hasText(last)) return first + " " + last;
        if (StringUtils.hasText(first)) return first;
        if (StringUtils.hasText(last)) return last;
        return null;
    }

    private List<String> parseCommaSeparatedList(String value) {
        if (!StringUtils.hasText(value)) return null;
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private record ProcessResult(String matchKey, ImportType importType,
                                 RecordStatus status, Long entityId,
                                 List<FieldChange> changes) {
    }
}