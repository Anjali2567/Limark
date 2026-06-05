package ai.leadplus.application.vendorqueryparser;

import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.industries.IndustryService;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.specifications.SpecificationService;
import ai.leadplus.infrastructure.springai.SpringAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorQueryParser {

    private final SpringAiClient springAiClient;
    private final ServiceCatalogueService serviceCatalogueService;
    private final IndustryService industryService;
    private final SpecificationService specificationService;

    private static final String VENDOR_QUERY_PARSING_PROMPT =
            FileReader.readFileContentFromClasspath("prompts/vendor-query-parser.md");

    public VendorSearchParseDto parseQuery(String userInput) {
        List<String> serviceNames = serviceCatalogueService.getAllActiveServiceNames();
        List<String> industryNames = industryService.getAllActiveIndustryNames();
        List<String> specificationNames = specificationService.getAllActiveSpecificationNames();

        String systemPrompt = VENDOR_QUERY_PARSING_PROMPT
                .replace("{{SERVICE_CATEGORY_LIST}}", String.join(", ", serviceNames))
                .replace("{{INDUSTRY_LIST}}", String.join(", ", industryNames))
                .replace("{{SPECIFICATION_LIST}}", String.join(", ", specificationNames));

        VendorQueryClassification classification = springAiClient.getChatCompletion(
                userInput,
                systemPrompt,
                VendorQueryClassification.class
        );

        if (classification == null) {
            return VendorSearchParseDto.builder()
                    .serviceIds(Collections.emptyList())
                    .industryIds(Collections.emptyList())
                    .specificationIds(Collections.emptyList())
                    .build();
        }

        List<Long> serviceIds = CollectionUtils.isEmpty(classification.getServiceCategories())
                ? Collections.emptyList()
                : serviceCatalogueService.findIdsByNames(classification.getServiceCategories())
                .stream().toList();

        List<Long> resolvedIndustryIds = CollectionUtils.isEmpty(classification.getIndustryNames())
                ? Collections.emptyList()
                : industryService.findIndustryIdsByNames(classification.getIndustryNames());

        List<Long> resolvedSpecificationIds = CollectionUtils.isEmpty(classification.getSpecificationNames())
                ? Collections.emptyList()
                : specificationService.findSpecificationIdsByNames(classification.getSpecificationNames())
                .stream().toList();

        return VendorSearchParseDto.builder()
                .serviceIds(serviceIds)
                .industryIds(resolvedIndustryIds)
                .specificationIds(resolvedSpecificationIds)
                .build();
    }
}
