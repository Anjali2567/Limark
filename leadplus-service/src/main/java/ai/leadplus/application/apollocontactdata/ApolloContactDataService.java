package ai.leadplus.application.apollocontactdata;

import ai.leadplus.domain.apollocontactdata.ApolloContactData;
import ai.leadplus.domain.apollocontactdata.ApolloContactDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApolloContactDataService {

    private final ApolloContactDataRepository apolloContactDataRepository;

    public void createApolloContactData(ApolloContactDataDto apolloContactDataDto) {
        ApolloContactData apolloContactData = apolloContactDataDto.toEntity();
        apolloContactDataRepository.save(apolloContactData);
    }
}
