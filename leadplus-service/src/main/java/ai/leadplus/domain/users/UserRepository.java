package ai.leadplus.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmailAndActiveTrue(String email);

    Optional<User> findByVerificationTokenAndActiveTrue(String token);

    List<User> findAllByIdInAndActiveTrue(Collection<Long> ids);

    Optional<User> findByEmailVerificationTokenAndActiveTrue(String token);

    List<User> findAllByIdInAndActiveAndDraftFalse(List<Long> ids, boolean active);

    long countByTenantIdAndActiveTrue(Long tenantId);
}
