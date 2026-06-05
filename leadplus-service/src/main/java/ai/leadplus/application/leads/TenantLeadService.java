package ai.leadplus.application.leads;

import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import ai.leadplus.application.leadcompany.LeadCompanyDto;
import ai.leadplus.application.leadcompany.LeadCompanySearchService;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcompany.LeadCompanyWithContactCountDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import ai.leadplus.domain.leadcompanies.LeadCompany;
import ai.leadplus.domain.leadcompanies.LeadCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantLeadService {

    private final VendorDataPackService vendorDataPackService;
    private final LeadDataPackService leadDataPackService;
    private final LeadCompanySearchService leadCompanySearchService;
    private final LeadCompanyService leadCompanyService;
    private final LeadContactService leadContactService;
    private final CompanyLeadSearchService companyLeadSearchService;
    private final ContactLeadSearchService contactLeadSearchService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final LeadCompanyRepository leadCompanyRepository;
    private final LeadService leadService;

    public LeadDto getContact(Long tenantId, Long contactId) {
        LeadDto leadDto = leadContactService.getLeadByContactId(contactId);
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            if (leadCompanySearchService.isCompanyInaccessible(leadDto.getCompanySegments(), leadDto.getCompanyTenantIds(), gatedInfo, accessOpt.get())) {
                throw new ResourceNotFoundException("Contact not found");
            }
        }
        return leadDto;
    }

    public LeadCompanyWithContactCountDto getCompany(Long tenantId, String idOrDomain) {
        LeadCompanyWithContactCountDto companyDto = leadCompanyService.getCompanyWithContactCount(idOrDomain);
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            if (leadCompanySearchService.isCompanyInaccessible(companyDto.getSegments(), companyDto.getTenantIds(), gatedInfo, accessOpt.get())) {
                throw new ResourceNotFoundException("Company not found");
            }
        }
        return companyDto;
    }

    public Page<LeadDto> getContactsByCompany(Long tenantId, String idOrDomain, Pageable pageable) {
        LeadCompanyDto company = leadCompanyService.getCompanyByIdOrDomain(idOrDomain);
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            // Gate both the company and its contacts
            if (leadCompanySearchService.isCompanyInaccessible(company.getSegments(), company.getTenantIds(), gatedInfo, accessOpt.get())) {
                return Page.empty(pageable);
            }
            return contactLeadSearchService.searchLeads(
                    tenantId, new LeadFilterCriteria(), null,
                    List.of(company.getId()), gatedInfo, accessOpt.get(), pageable);
        }
        return leadContactService.getLeadByCompanyIdOrDomainAndContactId(idOrDomain, pageable);
    }

    public Page<LeadDto> searchContacts(Long tenantId, LeadFilterCriteria req, String query,
                                        List<Long> companyIds, Pageable pageable) {
        if (req.isCampaignEligibleOnly()) {
            req.setExcludeContactIds(contactOutreachStatusService.getIneligibleContactIds(tenantId));
        }

        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            return contactLeadSearchService.searchLeads(tenantId, req, query, companyIds, gatedInfo, accessOpt.get(), pageable);
        }
        return contactLeadSearchService.searchLeads(tenantId, req, query, companyIds, pageable);
    }

    public Page<LeadCompanyWithContactCountDto> searchCompanies(
            Long tenantId,
            LeadFilterCriteria req,
            String query,
            Pageable pageable) {

        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);

        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            return companyLeadSearchService.searchLeadCompanies(req, query, gatedInfo, accessOpt.get(), pageable);
        }

        return companyLeadSearchService.searchLeadCompanies(req, query, null, null, pageable);
    }

    public List<Long> searchContactIds(Long tenantId, LeadFilterCriteria req, String query, List<Long> companyIds) {
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            return contactLeadSearchService.findContactIds(req, query, companyIds, tenantId, gatedInfo, accessOpt.get());
        }
        return contactLeadSearchService.findContactIds(req, query, companyIds, tenantId, null, null);
    }

    public long countMatchingCompanies(Long tenantId, LeadFilterCriteria req, String query, List<Long> companyIds) {
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            return contactLeadSearchService.countMatchingCompanies(req, query, companyIds, tenantId, gatedInfo, accessOpt.get());
        }
        return contactLeadSearchService.countMatchingCompanies(req, query, companyIds, tenantId, null, null);
    }

    public List<CompanyIdWithDomainDto> searchCompanyIdsWithDomains(Long tenantId, LeadFilterCriteria req, String query) {
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            return companyLeadSearchService.findCompanyIdsWithDomains(req, query, gatedInfo, accessOpt.get());
        }
        return companyLeadSearchService.findCompanyIdsWithDomains(req, query, null, null);
    }

    public List<CompanyLookupDto> lookupCompanies(Long tenantId, String query) {
        Specification<LeadCompany> lookupSpec = (root, cq, cb) -> {
            if (query != null && !query.isBlank()) {
                String pattern = query.toLowerCase() + "%";
                return cb.and(
                        cb.isTrue(root.get("active")),
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern),
                                cb.like(cb.lower(root.get("domain")), pattern)
                        )
                );
            }
            return cb.isTrue(root.get("active"));
        };

        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        Specification<LeadCompany> finalSpec;
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, accessOpt.get());
            finalSpec = gateSpec != null ? lookupSpec.and(gateSpec) : lookupSpec;
        } else {
            finalSpec = lookupSpec;
        }

        List<LeadCompany> companies = leadCompanyRepository.findAll(finalSpec, PageRequest.of(0, 20)).getContent();
        return companies.stream()
                .map(c -> new CompanyLookupDto(c.getId(), c.getName(), c.getDomain()))
                .toList();
    }

    public LeadStatisticsDto getStatistics(Long tenantId) {
        Optional<VendorAccess> accessOpt = vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId);
        if (accessOpt.isPresent()) {
            GatedInfo gatedInfo = leadDataPackService.getGatedInfo();
            Specification<LeadCompany> gateSpec = leadCompanySearchService.buildDataPackGateSpecification(gatedInfo, accessOpt.get());
            return leadService.getGatedStatistics(gateSpec, gatedInfo, accessOpt.get());
        }
        return leadService.getStatistics();
    }
}
