package ai.leadplus.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @Query(value = "SELECT DISTINCT q.* FROM question q WHERE q.active = true AND EXISTS (SELECT 1 FROM unnest(q.industry_ids) AS uid WHERE uid IN (:industryIds))", nativeQuery = true)
    List<Question> findByIndustryIdsInAndActiveTrue(@Param("industryIds") List<Long> industryIds);

    @Query(value = "SELECT DISTINCT q.id FROM question q WHERE q.active = true AND EXISTS (SELECT 1 FROM unnest(q.industry_ids) AS uid WHERE uid IN (:industryIds))", nativeQuery = true)
    List<Long> findIdsByIndustryIdsIn(@Param("industryIds") List<Long> industryIds);

    Optional<Question> findByIdAndActiveTrue(Long id);

    long countAllByIdInAndActiveTrue(List<Long> ids);
}
