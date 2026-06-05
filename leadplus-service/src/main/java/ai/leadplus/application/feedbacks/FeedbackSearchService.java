package ai.leadplus.application.feedbacks;

import ai.leadplus.domain.feedbacks.Feedback;
import ai.leadplus.domain.feedbacks.FeedbackRepository;
import ai.leadplus.domain.feedbacks.FeedbackStatus;
import ai.leadplus.domain.feedbacks.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackSearchService {
    private final FeedbackRepository feedbackRepository;

    public Page<Feedback> searchFeedbacks(String workspaceId,
                                          String userId,
                                          List<FeedbackStatus> statuses,
                                          List<FeedbackType> types,
                                          String query,
                                          Pageable pageable) {
        Specification<Feedback> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            String searchPattern = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("username")), searchPattern),
                            cb.like(cb.lower(root.get("companyName")), searchPattern),
                            cb.like(cb.lower(root.get("message")), searchPattern)
                    )
            );
        }

        if (StringUtils.hasText(workspaceId)) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("workspaceId"), workspaceId));
        }

        if (StringUtils.hasText(userId)) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), userId));
        }

        if (!CollectionUtils.isEmpty(statuses)) {
            spec = spec.and((root, q, cb) -> root.get("status").in(statuses));
        }

        if (!CollectionUtils.isEmpty(types)) {
            spec = spec.and((root, q, cb) -> root.get("type").in(types));
        }

        return feedbackRepository.findAll(spec, pageable);
    }
}