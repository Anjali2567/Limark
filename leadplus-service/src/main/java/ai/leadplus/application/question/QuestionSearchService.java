package ai.leadplus.application.question;

import ai.leadplus.domain.question.Question;
import ai.leadplus.domain.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionSearchService {

    private final QuestionRepository questionRepository;

    public Page<Question> searchQuestions(
            List<Long> questionIds,
            List<Long> industryIds,
            Pageable pageable) {

        Specification<Question> spec = Specification.where(null);

        if (!CollectionUtils.isEmpty(questionIds)) {
            spec = spec.and((root, query, cb) -> root.get("id").in(questionIds));
        }

        if (!CollectionUtils.isEmpty(industryIds)) {
            // industryIds is a PostgreSQL array column (@JdbcTypeCode(SqlTypes.ARRAY)), not a JPA
            // association, so root.join("industryIds") fails at the metamodel level. Pre-fetch
            // matching IDs via the JPQL query and filter by ID instead.
            List<Long> matchingIds = questionRepository.findIdsByIndustryIdsIn(industryIds);
            if (matchingIds.isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.disjunction());
            } else {
                spec = spec.and((root, query, cb) -> root.get("id").in(matchingIds));
            }
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        return questionRepository.findAll(spec, pageable);
    }
}
