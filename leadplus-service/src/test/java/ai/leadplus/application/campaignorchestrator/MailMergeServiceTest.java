package ai.leadplus.application.campaignorchestrator;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaignemails.CampaignEmailDto;
import ai.leadplus.application.contactemails.ContactEmailDto;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leads.TenantContactMetadataDto;
import ai.leadplus.application.users.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailMergeServiceTest {

    @Mock
    private LeadCompanyService leadCompanyService;

    @InjectMocks
    private MailMergeService mailMergeService;

    @Test
    void replacePlaceHolders_shouldResolveMixedCaseAliasesAndJoinPhoneNumbers() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder()
                .name("Acme Labs")
                .industry("Healthcare")
                .build();
        when(leadCompanyService.getCompanyByIdOrDomain("42")).thenReturn(leadCompanyDto);

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .leadCompanyId(42L)
                .firstName("Jordan")
                .bdName("Brooke")
                .isrName("Casey")
                .bdPhone("+1 555 0200")
                .isrPhone("+1 555 0300")
                .phoneE164("+1 555 0001")
                .build();

        CampaignEmailDto campaignEmail = CampaignEmailDto.builder()
                .subject("Hi {BdName}")
                .bodyTemplate(
                        "From {isrNAME} at {SENDERNAME}, call {senderNUMBER}, {bdPhoneNUMBER}, {isrPhoneNUMBER} for {companyname} in {INDUSTRY}."
                )
                .attachmentIds(List.of("a1"))
                .build();

        ContactEmailDto merged = mailMergeService.executeMailMerge(campaignContact, campaignEmail, "Taylor Smith", "+1 555 7777");

        assertThat(merged.getSubject()).isEqualTo("Hi Brooke");
        assertThat(merged.getBody()).isEqualTo(
                "From Casey at Taylor Smith, call +1 555 7777, +1 555 0200, +1 555 0300 for Acme Labs in Healthcare."
        );
    }

    @Test
    void replacePlaceHolders_shouldFallbackToEmptyStringsWhenValuesAreMissing() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder().build();

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .leadCompanyId(7L)
                .build();

        String result = mailMergeService.replacePlaceHolders(
                "Lead {bdNAME} {isrName} {SenderName} {senderNumber} {bdPhoneNumber} {isrPhoneNumber} {companyName} {industry} {firstName}",
                campaignContact,
                leadCompanyDto,
                null,
                null
        );

        assertThat(result.trim()).isEqualTo("Lead");
        assertThat(result).doesNotContain("{").doesNotContain("null");
    }

    @Test
    void replacePlaceHolders_shouldResolveBracketAndSpacedAliases() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder()
                .name("Acme Labs")
                .industry("Healthcare")
                .build();
        when(leadCompanyService.getCompanyByIdOrDomain("42")).thenReturn(leadCompanyDto);

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .leadCompanyId(42L)
                .firstName("Jordan")
                .bdName("Brooke")
                .isrName("Casey")
                .bdPhone("+1 555 0200")
                .isrPhone("+1 555 0300")
                .build();

        CampaignEmailDto campaignEmail = CampaignEmailDto.builder()
                .subject("[First Name] at [Company Name]")
                .bodyTemplate(
                        "{first name} [company name] [BD Name] {ISR Name} [Sender Name] {bd phone number} [isr phone number] {Industry}"
                )
                .attachmentIds(List.of())
                .build();

        ContactEmailDto merged = mailMergeService.executeMailMerge(campaignContact, campaignEmail, "Taylor Smith", "+1 555 7777");

        assertThat(merged.getSubject()).isEqualTo("Jordan at Acme Labs");
        assertThat(merged.getBody()).isEqualTo(
                "Jordan Acme Labs Brooke Casey Taylor Smith +1 555 0200 +1 555 0300 Healthcare"
        );
    }

    @Test
    void replacePlaceHolders_shouldResolveUnderscoreAliases() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder()
                .name("Acme Labs")
                .industry("Healthcare")
                .build();
        when(leadCompanyService.getCompanyByIdOrDomain("42")).thenReturn(leadCompanyDto);

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .leadCompanyId(42L)
                .firstName("Jordan")
                .bdName("Brooke")
                .isrName("Casey")
                .bdPhone("+1 555 0200")
                .isrPhone("+1 555 0300")
                .build();

        CampaignEmailDto campaignEmail = CampaignEmailDto.builder()
                .subject("[First_Name] at {Company_Name}")
                .bodyTemplate(
                        "[company_name] {bd_name} [isr_name] {sender_name} [sender_number] {bd_phone_number} [isr_phone_number] [industry]"
                )
                .attachmentIds(List.of())
                .build();

        ContactEmailDto merged = mailMergeService.executeMailMerge(campaignContact, campaignEmail, "Taylor Smith", "+1 555 7777");

        assertThat(merged.getSubject()).isEqualTo("Jordan at Acme Labs");
        assertThat(merged.getBody()).isEqualTo(
                "Acme Labs Brooke Casey Taylor Smith +1 555 7777 +1 555 0200 +1 555 0300 Healthcare"
        );
    }

    @Test
    void replacePlaceHolders_shouldResolveCamelCaseInBrackets() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder()
                .name("Acme Labs")
                .build();
        when(leadCompanyService.getCompanyByIdOrDomain("42")).thenReturn(leadCompanyDto);

        CampaignContactInfoDto campaignContact = CampaignContactInfoDto.builder()
                .leadCompanyId(42L)
                .firstName("Jordan")
                .build();

        CampaignEmailDto campaignEmail = CampaignEmailDto.builder()
                .subject("[firstName] at [companyName]")
                .bodyTemplate("[CompanyName] {FirstName}")
                .attachmentIds(List.of())
                .build();

        ContactEmailDto merged = mailMergeService.executeMailMerge(campaignContact, campaignEmail, "Taylor Smith", null);

        assertThat(merged.getSubject()).isEqualTo("Jordan at Acme Labs");
        assertThat(merged.getBody()).isEqualTo("Acme Labs Jordan");
    }

    @Test
    void replacePlaceHolders_shouldResolveDirectEmailPlaceholders() {
        LeadCompanyDto leadCompanyDto = LeadCompanyDto.builder()
                .name("Acme Labs")
                .industry("Healthcare")
                .build();
        when(leadCompanyService.getCompanyByIdOrDomain("77")).thenReturn(leadCompanyDto);

        UserDto senderUser = UserDto.builder()
                .name("Taylor Smith")
                .email("taylor@example.com")
                .phoneNumber("+1 555 7777")
                .build();

        TenantContactMetadataDto metadata = TenantContactMetadataDto.builder()
                .bdName("Brooke")
                .bdPhone("+1 555 0200")
                .isrName("Casey")
                .isrPhone("+1 555 0300")
                .build();

        LeadContactDto leadContact = LeadContactDto.builder()
                .leadCompanyId(77L)
                .firstName("Jordan")
                .build();

        ContactEmailDto contactEmail = ContactEmailDto.builder()
                .subject("Hi {FIRSTNAME}")
                .body("{senderNAME} {senderNUMBER} {bdNAME} {isrNAME} {bdPhoneNUMBER} {isrPhoneNUMBER}")
                .build();

        ContactEmailDto merged = mailMergeService.executeMailMerge(leadContact, contactEmail, senderUser, metadata);

        assertThat(merged.getSubject()).isEqualTo("Hi Jordan");
        assertThat(merged.getBody()).isEqualTo(
                "Taylor Smith +1 555 7777 Brooke Casey +1 555 0200 +1 555 0300"
        );
    }
}
