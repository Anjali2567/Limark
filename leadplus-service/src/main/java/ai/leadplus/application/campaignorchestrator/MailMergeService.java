package ai.leadplus.application.campaignorchestrator;


import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaignemails.CampaignEmailDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leads.TenantContactMetadataDto;
import ai.leadplus.application.users.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MailMergeService {

    @Value("${mail-guard.enabled:true}")
    private boolean isMailGuardEnabled;

    private final LeadCompanyService leadCompanyService;

    private final static List<String> EMAILS = List.of("nethma+campaign@limarktech.com");

    public ContactEmailDto executeMailMerge(CampaignContactInfoDto campaignContact, CampaignEmailDto campaignEmail, String senderName, String senderNumber) {

        LeadCompanyDto leadCompany = leadCompanyService.getCompanyByIdOrDomain(String.valueOf(campaignContact.getLeadCompanyId()));

        List<RecipientDto> toRecipients = isMailGuardEnabled ?
                List.of(new RecipientDto(EMAILS.getFirst(), campaignContact.getFirstNameNormalized())) :
                List.of(new RecipientDto(campaignContact.getEmail(), campaignContact.getFirstNameNormalized()));

        String subject = campaignEmail.getSubject();
        String body = campaignEmail.getBodyTemplate();
        subject = replacePlaceHolders(subject, campaignContact, leadCompany, senderName, senderNumber);
        body = replacePlaceHolders(body, campaignContact, leadCompany, senderName, senderNumber);

        return ContactEmailDto.builder()
                .toRecipients(toRecipients)
                .subject(subject)
                .body(body)
                .attachmentIds(campaignEmail.getAttachmentIds())
                .build();
    }

    public ContactEmailDto executeMailMerge(LeadContactDto leadContactDto, ContactEmailDto contactEmailDto, UserDto senderUser, TenantContactMetadataDto tenantContactMetadata) {
        LeadCompanyDto leadCompany = leadCompanyService.getCompanyByIdOrDomain(String.valueOf(leadContactDto.getLeadCompanyId()));

        String subject = replacePlaceHolders(contactEmailDto.getSubject(), leadContactDto, leadCompany, senderUser, tenantContactMetadata);
        String body = replacePlaceHolders(contactEmailDto.getBody(), leadContactDto, leadCompany, senderUser, tenantContactMetadata);

        Long workspaceId = contactEmailDto.getWorkspaceId();
        Long contactId = contactEmailDto.getContactId();
        Long tenantId = contactEmailDto.getTenantId();

        List<RecipientDto> ccRecipients = contactEmailDto.getCcRecipients();
        List<RecipientDto> bccRecipients = contactEmailDto.getBccRecipients();
        List<RecipientDto> toRecipients = isMailGuardEnabled ?
                List.of(new RecipientDto(EMAILS.getFirst(), leadContactDto.getFirstNameNormalized())) :
                contactEmailDto.getToRecipients();

        return ContactEmailDto.builder()
                .toRecipients(toRecipients)
                .ccRecipients(ccRecipients)
                .bccRecipients(bccRecipients)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .contactId(contactId)
                .subject(subject)
                .body(body)
                .attachmentIds(contactEmailDto.getAttachmentIds())
                .build();
    }

    public String replacePlaceHolders(String template, CampaignContactInfoDto campaignContact, LeadCompanyDto leadCompany, String senderName, String senderNumber) {
        if (template == null) {
            return "";
        }
        return replacePlaceholders(template, buildPlaceholderValues(
                safeString(campaignContact.getFirstName()),
                leadCompany,
                safeString(campaignContact.getBdName()),
                safeString(campaignContact.getIsrName()),
                senderName,
                senderNumber,
                campaignContact.getBdPhone(),
                campaignContact.getIsrPhone()
        ));
    }

    public String replacePlaceHolders(String template, LeadContactDto leadContactDto, LeadCompanyDto leadCompany, UserDto senderUser, TenantContactMetadataDto tenantContactMetadata) {
        if (template == null) {
            return "";
        }
        String senderName = senderUser != null
                ? (StringUtils.hasText(senderUser.getName()) ? senderUser.getName() : senderUser.getEmail())
                : null;
        String senderNumber = senderUser != null ? senderUser.getPhoneNumber() : null;
        return replacePlaceholders(template, buildPlaceholderValues(
                safeString(leadContactDto.getFirstName()),
                leadCompany,
                safeString(getMetadataValue(tenantContactMetadata, TenantContactMetadataDto::getBdName)),
                safeString(getMetadataValue(tenantContactMetadata, TenantContactMetadataDto::getIsrName)),
                senderName,
                senderNumber,
                getMetadataValue(tenantContactMetadata, TenantContactMetadataDto::getBdPhone),
                getMetadataValue(tenantContactMetadata, TenantContactMetadataDto::getIsrPhone)
        ));
    }

    private Map<String, String> buildPlaceholderValues(
            String firstName,
            LeadCompanyDto leadCompany,
            String bdName,
            String isrName,
            String senderName,
            String senderNumber,
            String bdPhoneNumber,
            String isrPhoneNumber
    ) {
        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("firstName", safeString(firstName));
        replacements.put("companyName", safeString(leadCompany.getName()));
        replacements.put("industry", safeString(leadCompany.getIndustry()));
        replacements.put("bdName", safeString(bdName));
        replacements.put("isrName", safeString(isrName));
        replacements.put("isName", safeString(isrName));
        replacements.put("senderName", safeString(senderName));
        replacements.put("senderNumber", safeString(senderNumber));
        replacements.put("bdPhoneNumber", safeString(bdPhoneNumber));
        replacements.put("isrPhoneNumber", safeString(isrPhoneNumber));
        return replacements;
    }

    private String getMetadataValue(TenantContactMetadataDto metadata, java.util.function.Function<TenantContactMetadataDto, String> extractor) {
        return metadata != null ? extractor.apply(metadata) : null;
    }

    private String replacePlaceholders(String template, Map<String, String> replacements) {
        String result = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = replacePlaceholderIgnoreCase(result, entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Replaces a placeholder in the template with the given replacement value.
     * Matching is case-insensitive and flexible with delimiters, spacing, and separators.
     *
     * <p>For a placeholder key like {@code "companyName"}, all the following will match:
     * <ul>
     *   <li>{@code {companyName}} — camelCase in braces</li>
     *   <li>{@code [companyName]} — camelCase in brackets</li>
     *   <li>{@code {Company Name}} — spaced, any case</li>
     *   <li>{@code [Company_Name]} — underscored, any case</li>
     *   <li>{@code {COMPANYNAME}} — no separator, any case</li>
     *   <li>{@code { company_name }} — padded whitespace/underscores</li>
     * </ul>
     */
    private String replacePlaceholderIgnoreCase(String template, String placeholder, String replacement) {
        String[] words = placeholder.split("(?=[A-Z])");
        String flexibleKey = String.join("[\\s_]*", words);
        Pattern pattern = Pattern.compile("[{\\[][\\s_]*" + flexibleKey + "[\\s_]*[}\\]]", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(template).replaceAll(Matcher.quoteReplacement(safeString(replacement)));
    }

    private String safeString(String value) {
        return StringUtils.hasText(value) ? value : "";
    }
}
