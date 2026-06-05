package ai.leadplus.application.apollo;

import ai.leadplus.application.apollocompanydata.ApolloCompanyDataDto;
import ai.leadplus.application.apollocompanydata.ApolloCompanyDataService;
import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import ai.leadplus.application.apollospecification.ApolloSpecificationService;
import ai.leadplus.application.exception.InvalidJsonFormatException;
import ai.leadplus.application.leadcompany.ApolloDataFetchedEvent;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.domain.common.ApolloDataType;
import ai.leadplus.infrastructure.apollo.ApolloOrganizationEnrichClient;
import ai.leadplus.infrastructure.apollo.ApolloSearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloLeadCompanyService {

    private final LeadCompanyService leadCompanyService;
    private final ApolloSpecificationService apolloSpecificationService;
    private final ApolloSearchClient apolloSearchClient;
    private final ApolloOrganizationEnrichClient apolloOrganizationEnrichClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ApolloCompanyDataService apolloCompanyDataService;
    private final ObjectMapper objectMapper;

    public void executePeopleSearchApollo(String companyIdOrDomain) {
        LeadCompanyDto leadCompanyDto = leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
        if (StringUtils.hasText(leadCompanyDto.getDomain())) {
            ApolloSpecificationDto specificationDto = apolloSpecificationService.getLatestSpecification();
            String peopleData = apolloSearchClient.searchPeople(leadCompanyDto.getDomain(), specificationDto);
            ApolloCompanyDataDto apolloCompanyDataDto = ApolloCompanyDataDto.builder()
                    .leadCompanyId(leadCompanyDto.getId())
                    .type(ApolloDataType.PEOPLE_SEARCH)
                    .data(peopleData)
                    .specificationId(specificationDto.getId())
                    .fetchedAt(LocalDateTime.now())
                    .build();
            apolloCompanyDataService.createApolloCompanyData(apolloCompanyDataDto);
            leadCompanyService.saveLeadCompany(leadCompanyDto);
            ApolloDataFetchedEvent apolloDataFetchedEvent = new ApolloDataFetchedEvent(this, leadCompanyDto, apolloCompanyDataDto);
            eventPublisher.publishEvent(apolloDataFetchedEvent);
        }
    }

    public void enrichCompanyWithApolloOrganizationData(String companyIdOrDomain) {
        LeadCompanyDto leadCompanyDto = leadCompanyService.getCompanyByIdOrDomain(companyIdOrDomain);
        String domain = leadCompanyDto.getDomain();
        if (!StringUtils.hasText(domain)) {
            log.warn("Company with ID {} does not have a valid domain for Apollo enrichment", leadCompanyDto.getId());
            return;
        }
        String organizationData = apolloOrganizationEnrichClient.enrichOrganizationByDomain(domain);

        try {
            JsonNode rootNode = objectMapper.readTree(organizationData);
            if (rootNode.isEmpty()) {
                log.info("Apollo returned empty organization data for domain: {}", domain);
                return;
            }

            JsonNode organizationNode = rootNode.path("organization");
            if (organizationNode.isMissingNode() || organizationNode.isNull()) {
                log.warn("No organization data found for domain: {}", domain);
                return;
            }

            if (organizationNode.has("sic_codes")) {
                JsonNode sicCodesNode = organizationNode.get("sic_codes");
                if (sicCodesNode.isArray() && !sicCodesNode.isEmpty()) {
                    List<String> sicCodes = new ArrayList<>();
                    for (JsonNode sicCodeNode : sicCodesNode) {
                        sicCodes.add(sicCodeNode.asText());
                    }
                    leadCompanyDto.setSicCodes(sicCodes);
                }
            }

            if (organizationNode.has("naics_codes")) {
                JsonNode naicsCodesNode = organizationNode.get("naics_codes");
                if (naicsCodesNode.isArray() && !naicsCodesNode.isEmpty()) {
                    List<String> naicsCodes = new ArrayList<>();
                    for (JsonNode naicsCodeNode : naicsCodesNode) {
                        naicsCodes.add(naicsCodeNode.asText());
                    }
                    leadCompanyDto.setNaicsCodes(naicsCodes);
                }
            }

            if (organizationNode.has("publicly_traded_symbol")) {
                String publiclyTradedSymbol = organizationNode.get("publicly_traded_symbol").asText(null);
                leadCompanyDto.setPubliclyTradedSymbol(publiclyTradedSymbol);
            }

            if (organizationNode.has("twitter_url")) {
                String twitterUrl = organizationNode.get("twitter_url").asText(null);
                leadCompanyDto.setTwitterUrl(twitterUrl);
            }

            if (organizationNode.has("facebook_url")) {
                String facebookUrl = organizationNode.get("facebook_url").asText(null);
                leadCompanyDto.setFacebookUrl(facebookUrl);
            }

            if (organizationNode.has("phone")) {
                String phone = organizationNode.get("phone").asText(null);
                leadCompanyDto.setPhoneNumber(phone);
            }

            if (organizationNode.has("website_url")) {
                String websiteUrl = organizationNode.get("website_url").asText(null);
                leadCompanyDto.setWebsiteUrl(websiteUrl);
            }

            if (organizationNode.has("keywords")) {
                JsonNode keywordsNode = organizationNode.get("keywords");
                if (keywordsNode.isArray() && !keywordsNode.isEmpty()) {
                    List<String> keywords = new ArrayList<>();
                    for (JsonNode keyword : keywordsNode) {
                        keywords.add(keyword.asText());
                    }
                    leadCompanyDto.setKeywords(keywords);
                }
            }

            if (organizationNode.has("technology_names")) {
                JsonNode technologyNamesNode = organizationNode.get("technology_names");
                if (technologyNamesNode.isArray() && !technologyNamesNode.isEmpty()) {
                    List<String> technologies = new ArrayList<>();
                    for (JsonNode technology : technologyNamesNode) {
                        technologies.add(technology.asText());
                    }
                    leadCompanyDto.setTechnologies(technologies);
                }
            }

            if (organizationNode.has("annual_revenue")) {
                BigDecimal annualRevenue = organizationNode.get("annual_revenue").decimalValue();
                leadCompanyDto.setRevenueUsd(annualRevenue);
            }

            if (organizationNode.has("postal_code")) {
                String postalCode = organizationNode.get("postal_code").asText(null);
                leadCompanyDto.setPostalCode(postalCode);
            }

            if (organizationNode.has("logo_url")) {
                String logoUrl = organizationNode.get("logo_url").asText(null);
                leadCompanyDto.setLogoUrl(logoUrl);
            }

            leadCompanyService.saveLeadCompany(leadCompanyDto);
            ApolloCompanyDataDto apolloCompanyDataDto = ApolloCompanyDataDto.builder()
                    .leadCompanyId(leadCompanyDto.getId())
                    .type(ApolloDataType.ORGANIZATION_ENRICHMENT)
                    .data(organizationData)
                    .fetchedAt(LocalDateTime.now())
                    .build();
            apolloCompanyDataService.createApolloCompanyData(apolloCompanyDataDto);

        } catch (Exception e) {
            throw new InvalidJsonFormatException("Invalid organization data format");
        }
    }
}
