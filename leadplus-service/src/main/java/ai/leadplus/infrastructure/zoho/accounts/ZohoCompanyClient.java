package ai.leadplus.infrastructure.zoho.accounts;

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
public class ZohoCompanyClient {

    private final ZohoConfiguration zohoConfiguration;
    private final ZohoAuthClient zohoAuthClient;
    private final RestTemplate restTemplate;

    private static final String FIELDS = "Account_Name";

    public ZohoCompaniesResponse fetchCompanies(String refreshToken, int page, int perPage) {
        String url = zohoConfiguration.getApiDomain()
                + "/crm/v8/Accounts"
                + "?fields=" + FIELDS
                + "&page=" + page
                + "&per_page=" + perPage;

        HttpEntity<Void> request = new HttpEntity<>(zohoAuthClient.createHeader(refreshToken));

        ZohoCompaniesResponse responseBody = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ZohoCompaniesResponse.class
        ).getBody();

        if (responseBody == null) {
            throw new ServiceUnavailableException("Failed to fetch Zoho companies: empty response");
        }

        if (StringUtils.hasText(responseBody.getError())) {
            throw new ServiceUnavailableException("Failed to fetch Zoho companies: " + responseBody.getError());
        }

        return responseBody;
    }
}
