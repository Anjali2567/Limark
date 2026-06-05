package ai.leadplus.application.leadsearchhistories;

import ai.leadplus.domain.leadsearchhistories.LeadSearchHistory;
import ai.leadplus.domain.leadsearchhistories.LeadSearchHistoryRepository;
import ai.leadplus.domain.common.LeadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadSearchHistoryService {

    private final LeadSearchHistoryRepository leadSearchHistoryRepository;

    public LeadSearchHistoryDto createLeadSearchHistory(LeadSearchHistoryDto leadSearchHistoryDto) {
        LeadSearchHistory leadSearchHistory = leadSearchHistoryDto.toEntity();
        LeadSearchHistory savedLeadSearchHistory = leadSearchHistoryRepository.save(leadSearchHistory);
        return LeadSearchHistoryDto.fromEntity(savedLeadSearchHistory);
    }

    public List<LeadSearchHistoryDto> getLeadSearchHistoryByUserId(Long userId, LeadType type) {
        List<LeadSearchHistory> leadSearchHistory = leadSearchHistoryRepository.findByUserIdAndType(userId, type);
        return leadSearchHistory.stream().map(LeadSearchHistoryDto::fromEntity).collect(Collectors.toList());
    }
}
