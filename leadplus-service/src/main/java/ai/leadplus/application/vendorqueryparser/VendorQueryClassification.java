package ai.leadplus.application.vendorqueryparser;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
public class VendorQueryClassification {

    @JsonPropertyDescription("Service category names from the provided list that match the query. Maximum 3 items. Only include if confident.")
    private List<String> serviceCategories;

    @JsonPropertyDescription("Industry names from the provided list that match the query. Maximum 3 items. Only include if confident.")
    private List<String> industryNames;

    @JsonPropertyDescription("Specification/technology names from the provided list that match the query. Maximum 5 items. Only include if confident.")
    private List<String> specificationNames;

    @JsonPropertyDescription("2-3 technical keywords describing the core of the user's request (e.g. 'SAP implementation', 'cloud migration')")
    private List<String> keywords;

    @JsonPropertyDescription("Country name if explicitly mentioned (e.g. 'India', 'Philippines'). Use full name. Null if not mentioned.")
    private String country;

    @JsonPropertyDescription("City name if explicitly mentioned. Null if not mentioned.")
    private String city;
}
