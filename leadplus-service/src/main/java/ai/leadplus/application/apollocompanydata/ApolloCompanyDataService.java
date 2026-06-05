package ai.leadplus.application.apollocompanydata;

import ai.leadplus.domain.apollocompanydata.ApolloCompanyData;
import ai.leadplus.domain.apollocompanydata.ApolloCompanyDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApolloCompanyDataService {

    private final ApolloCompanyDataRepository apolloCompanyDataRepository;

    public void createApolloCompanyData(ApolloCompanyDataDto apolloCompanyDataDto) {
        ApolloCompanyData apolloCompanyData = apolloCompanyDataDto.toEntity();
        apolloCompanyDataRepository.save(apolloCompanyData);
    }
}
