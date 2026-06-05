package ai.leadplus.domain.collaborators;

import ai.leadplus.domain.common.SourcingRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {

    List<Collaborator> findAllBySourceIdAndSourceTypeAndActiveTrue(Long sourceId, SourcingRequestType sourceType);

    boolean existsBySourceIdAndSourceTypeAndUserIdAndActiveTrue(Long sourceId, SourcingRequestType sourceType, Long userId);

    Optional<Collaborator> findBySourceIdAndSourceTypeAndUserIdAndActiveTrue(Long sourceId, SourcingRequestType sourceType, Long userId);
}
