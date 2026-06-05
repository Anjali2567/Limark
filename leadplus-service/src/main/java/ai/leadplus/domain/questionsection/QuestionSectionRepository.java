package ai.leadplus.domain.questionsection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionSectionRepository extends JpaRepository<QuestionSection, Long> {

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    Optional<QuestionSection> findByIdAndActiveTrue(Long id);

    List<QuestionSection> findAllByIdInAndActiveTrue(Set<Long> ids);

    List<QuestionSection> findAllByActiveTrue();
}
