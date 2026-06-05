package ai.leadplus.domain.promptspecifications;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptSpecificationRepository extends JpaRepository<PromptSpecification, Long> {
    PromptSpecification findFirstByTypeOrderByCreatedAtDesc(PromptSpecificationType type);
}
