package ai.leadplus.application.apollospecification;

import ai.leadplus.domain.apollospecification.ApolloSpecification;
import ai.leadplus.domain.apollospecification.ApolloSpecificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApolloSpecificationService {

    private final ApolloSpecificationRepository apolloSpecificationRepository;

    public ApolloSpecificationDto getLatestSpecification() {
        return ApolloSpecificationDto.fromSpecification(findLatestSpecification());
    }

    public ApolloSpecification findLatestSpecification() {
        return apolloSpecificationRepository.findTopByOrderByCreatedAtDesc()
                .orElse(new ApolloSpecification());
    }

    public ApolloSpecificationDto updateSpecification(ApolloSpecificationDto dto) {
        ApolloSpecification savedSpecification = apolloSpecificationRepository.save(dto.toSpecification());
        return ApolloSpecificationDto.fromSpecification(savedSpecification);
    }
}
