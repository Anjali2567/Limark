package ai.leadplus.domain.apollospecification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApolloSpecificationRepository extends JpaRepository<ApolloSpecification, Long> {

    Optional<ApolloSpecification> findTopByOrderByCreatedAtDesc();
}
