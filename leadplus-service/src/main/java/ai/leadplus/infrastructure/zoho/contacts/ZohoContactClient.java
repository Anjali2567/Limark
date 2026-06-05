package ai.leadplus.infrastructure.zoho.contacts;

import ai.leadplus.application.exception.ServiceUnavailableException;
import ai.leadplus.infrastructure.zoho.ZohoConfiguration;
import ai.leadplus.infrastructure.zoho.auth.ZohoAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ZohoContactClient {

    private final ZohoConfiguration zohoConfiguration;
    private final ZohoAuthClient zohoAuthClient;
    private final RestTemplate restTemplate;

    private static final String FIELDS =
            "First_Name,Last_Name,Email,Title,Phone,Mobile,Other_City,Other_State,Other_Country,Other_Zip,Account_Name";

    public ZohoContactsResponse fetchContacts(String refreshToken, int page, int perPage) {

        String url = zohoConfiguration.getApiDomain()
                + "/crm/v8/Contacts"
                + "?fields=" + FIELDS
                + "&page=" + page
                + "&per_page=" + perPage;

        HttpEntity<Void> request = new HttpEntity<>(zohoAuthClient.createHeader(refreshToken));

        ZohoContactsResponse responseBody = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ZohoContactsResponse.class
        ).getBody();

        if(responseBody == null){
            throw new ServiceUnavailableException("Failed to fetch Zoho contacts: empty response");
        }

        if (StringUtils.hasText(responseBody.getError())){
            throw new ServiceUnavailableException("Failed to fetch Zoho contacts: " + responseBody.getError());
        }

        return responseBody;
    }
}