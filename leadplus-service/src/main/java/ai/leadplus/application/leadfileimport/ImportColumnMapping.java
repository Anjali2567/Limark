package ai.leadplus.application.leadfileimport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ImportColumnMapping {

    private ImportColumnMapping() {
    }

    public static final Map<String, String> CONTACT_COLUMN_MAP = new LinkedHashMap<>() {{
        put("CONTACT_EMAIL", "email");
        put("COMPANY_DOMAIN", "companyDomain");
        put("CONTACT_FIRST_NAME", "firstName");
        put("CONTACT_LAST_NAME", "lastName");
        put("CONTACT_TITLE", "title");
        put("CONTACT_SENIORITY", "seniority");
        put("CONTACT_DEPARTMENT", "department");
        put("CONTACT_PHONE_E164", "phoneE164");
        put("CONTACT_LINKEDIN_URL", "linkedinUrl");
        put("CONTACT_LOCATION_CITY", "locationCity");
        put("CONTACT_LOCATION_STATE", "locationState");
        put("CONTACT_LOCATION_COUNTRY", "locationCountry");
        put("CONTACT_LOCATION_ZIP", "locationZip");
        put("CONTACT_PERSONA_MATCH", "personaMatch");
        put("CONTACT_NOTES", "notes");
    }};

    public static final Map<String, String> COMPANY_COLUMN_MAP = new LinkedHashMap<>() {{
        put("COMPANY_DOMAIN", "domain");
        put("COMPANY_NAME", "name");
        put("COMPANY_WEBSITE_URL", "websiteUrl");
        put("COMPANY_LINKEDIN_URL", "linkedinUrl");
        put("COMPANY_TWITTER_URL", "twitterUrl");
        put("COMPANY_FACEBOOK_URL", "facebookUrl");
        put("COMPANY_PHONE", "phoneNumber");
        put("COMPANY_INDUSTRY", "industry");
        put("COMPANY_EMPLOYEE_COUNT", "employeeCount");
        put("COMPANY_HQ_CITY", "hqCity");
        put("COMPANY_HQ_STATE", "hqState");
        put("COMPANY_HQ_COUNTRY", "hqCountry");
        put("COMPANY_POSTAL_CODE", "postalCode");
        put("COMPANY_TERRITORY", "territory");
        put("COMPANY_REGION", "region");
        put("COMPANY_ICP_TAG", "icpTag");
        put("COMPANY_SEGMENT", "segments");
        put("COMPANY_ACCOUNT_SUMMARY", "accountSummary");
    }};

    public static final Set<String> CONTACT_ENRICHABLE_FIELDS = Set.of(
            "firstName", "lastName", "title", "seniority", "department",
            "phoneE164", "linkedinUrl", "locationCity", "locationState",
            "locationCountry", "locationZip", "personaMatch", "notes"
    );

    public static final Set<String> COMPANY_ENRICHABLE_FIELDS = Set.of(
            "name", "websiteUrl", "linkedinUrl", "twitterUrl", "facebookUrl",
            "phoneNumber", "industry", "employeeCount", "hqCity", "hqState",
            "hqCountry", "postalCode", "territory", "region", "icpTag",
            "segments", "accountSummary"
    );

    public static final String CONTACT_TEMPLATE_GUIDE =
            "Required columns: CONTACT_EMAIL, COMPANY_DOMAIN. " +
                    "Optional columns: CONTACT_FIRST_NAME, CONTACT_LAST_NAME, CONTACT_TITLE, " +
                    "CONTACT_SENIORITY, CONTACT_DEPARTMENT, CONTACT_PHONE_E164, CONTACT_LINKEDIN_URL, " +
                    "CONTACT_LOCATION_CITY, CONTACT_LOCATION_STATE, CONTACT_LOCATION_COUNTRY, " +
                    "CONTACT_LOCATION_ZIP, CONTACT_PERSONA_MATCH, CONTACT_NOTES.";

    public static final String COMPANY_TEMPLATE_GUIDE =
            "Required column: COMPANY_DOMAIN. " +
                    "Optional columns: COMPANY_NAME, COMPANY_WEBSITE_URL, COMPANY_LINKEDIN_URL, " +
                    "COMPANY_TWITTER_URL, COMPANY_FACEBOOK_URL, COMPANY_PHONE, COMPANY_INDUSTRY, " +
                    "COMPANY_EMPLOYEE_COUNT, COMPANY_HQ_CITY, COMPANY_HQ_STATE, COMPANY_HQ_COUNTRY, " +
                    "COMPANY_POSTAL_CODE, COMPANY_TERRITORY, COMPANY_REGION, COMPANY_ICP_TAG, " +
                    "COMPANY_SEGMENT, COMPANY_ACCOUNT_SUMMARY.";
}