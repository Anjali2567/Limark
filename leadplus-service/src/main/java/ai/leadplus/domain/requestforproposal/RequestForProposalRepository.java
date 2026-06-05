package ai.leadplus.domain.requestforproposal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestForProposalRepository extends JpaRepository<RequestForProposal, Long> {

    Optional<RequestForProposal> findByIdAndActiveTrue(Long id);

    Page<RequestForProposal> findAllByUserIdAndActiveTrue(Long userId, Pageable pageable);
}
