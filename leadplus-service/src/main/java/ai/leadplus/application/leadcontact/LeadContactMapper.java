package ai.leadplus.application.leadcontact;

import ai.leadplus.application.common.utils.StringNormalizer;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.domain.leadcontacts.DataSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LeadContactMapper {

    private LeadContactMapper() {
    }

    public static LeadContactDto mapFromApolloPerson(JsonNode personNode) {
        return mapFromApolloPerson(null, personNode);
    }

    public static LeadContactDto mapFromApolloPerson(LeadContactDto leadContact, JsonNode personNode) {
        if (leadContact == null) {
            leadContact = new LeadContactDto();
            leadContact.setDataSource(DataSource.APOLLO);
            leadContact.setSource("apollo");
            leadContact.setDoNotContact(false);
            leadContact.setActive(true);
        }

        setIfPresent(personNode, "id", leadContact::setApolloId);
        setIfPresentWithNormalization(personNode, "first_name", leadContact::setFirstName, leadContact::setFirstNameNormalized);
        setIfPresentWithNormalization(personNode, "last_name", leadContact::setLastName, leadContact::setLastNameNormalized);
        setIfPresent(personNode, "title", leadContact::setTitle);
        setIfPresent(personNode, "has_email", leadContact::setEmailStatus);
        setIfPresent(personNode, "linkedin_url", leadContact::setLinkedinUrl);
        setIfPresent(personNode, "city", leadContact::setLocationCity);
        setIfPresent(personNode, "state", leadContact::setLocationState);
        setIfPresent(personNode, "country", leadContact::setLocationCountry);
        setIfPresent(personNode, "postal_code", leadContact::setLocationZip);
        setIfPresent(personNode,"seniority", leadContact::setSeniority);

        if (personNode.has("email")) {
            String email = personNode.get("email").asText(null);
            if (!email.contains("email_not_unlocked@domain.com")) {
                leadContact.setEmail(email);
            }
        }

        if (personNode.has("departments")) {
            JsonNode departments = personNode.get("departments");
            if (departments.isArray() && !departments.isEmpty()) {
                String department = departments.get(0).asText(null);
                leadContact.setDepartment(department);
            }
        }

        String fullName = getContactFullName(leadContact.getFirstName(), leadContact.getLastName());
        leadContact.setFullName(fullName);

        return leadContact;
    }

    public static String getContactFullName(String firstName, String lastName) {
        String fullName = Stream.of(firstName, lastName)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));

        fullName = StringNormalizer.normalize(fullName);

        if (!StringUtils.hasText(fullName)) {
            throw new BadRequestException("First or Last name are required");
        }

        return StringNormalizer.normalizeEachWord(fullName);
    }

    private static void setIfPresent(JsonNode node, String field, Consumer<String> setter) {
        if (node.has(field)) {
            String value = node.get(field).asText(null);
            if (StringUtils.hasText(value)) {
                setter.accept(value);
            }
        }
    }

    private static void setIfPresentWithNormalization(JsonNode node, String field,
                                                      Consumer<String> setter,
                                                      Consumer<String> normalizedSetter) {
        if (node.has(field)) {
            String value = node.get(field).asText(null);
            if (StringUtils.hasText(value)) {
                setter.accept(value);
                normalizedSetter.accept(StringNormalizer.normalize(value));
            }
        }
    }
}
