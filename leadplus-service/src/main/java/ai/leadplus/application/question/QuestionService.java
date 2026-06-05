package ai.leadplus.application.question;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.questionsection.QuestionSectionDto;
import ai.leadplus.application.questionsection.QuestionSectionService;
import ai.leadplus.domain.question.Question;
import ai.leadplus.domain.question.QuestionRepository;
import ai.leadplus.domain.question.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionSearchService questionSearchService;
    private final QuestionSectionService questionSectionService;

    public QuestionDto createQuestion(QuestionDto questionDto) {
        questionSectionService.findQuestionSectionById(questionDto.getQuestionSectionId());
        validateQuestionOptions(questionDto);
        Question question = questionDto.toEntity();
        question.setActive(true);
        Question savedEntity = questionRepository.save(question);
        return QuestionDto.toDto(savedEntity);
    }

    public QuestionDto updateQuestion(String questionId, QuestionDto questionDto) {
        QuestionDto questionnaire = getQuestionById(questionId);
        validateQuestionOptions(questionDto);
        questionSectionService.findQuestionSectionById(questionDto.getQuestionSectionId());
        questionnaire.setQuestionSectionId(questionDto.getQuestionSectionId());
        questionnaire.setIndustryIds(questionDto.getIndustryIds());
        questionnaire.setLabel(questionDto.getLabel());
        questionnaire.setType(questionDto.getType());
        questionnaire.setOptions(questionDto.getOptions());
        questionnaire.setPosition(questionDto.getPosition());
        return QuestionDto.toDto(questionRepository.save(questionnaire.toEntity()));
    }

    public void deleteQuestionById(String questionId) {
        QuestionDto question = getQuestionById(questionId);
        questionRepository.delete(question.toEntity());
    }

    public Page<QuestionDto> getAllQuestions(List<Long> questionIds, List<Long> industryIds, Pageable pageable) {
        Page<Question> questionPage = questionSearchService.searchQuestions(questionIds, industryIds, pageable);
        return questionPage.map(QuestionDto::toDto);
    }

    public List<SectionWithQuestionDto> getQuestionnaireWithSectionByIndustryId(Long industryId) {
        List<QuestionDto> questionDtos = getAllQuestionsByIndustryId(industryId);
        List<QuestionSectionDto> questionSectionDtos = questionDtos.stream()
                .map(questionDto -> questionSectionService
                        .getQuestionSectionById(questionDto.getQuestionSectionId()))
                .distinct()
                .toList();

        Map<Long, List<QuestionDto>> questionsBySection = questionDtos.stream()
                .collect(Collectors.groupingBy(QuestionDto::getQuestionSectionId));

        return questionSectionDtos.stream()
                .map(
                        section -> {
                            List<QuestionDto> questions = questionsBySection
                                    .getOrDefault(section.getId(), Collections.emptyList())
                                    .stream()
                                    .sorted(Comparator.comparing(
                                            QuestionDto::getPosition,
                                            Comparator.nullsLast(Comparator.naturalOrder())
                                    ))
                                    .toList();

                            return SectionWithQuestionDto.builder()
                                    .sectionId(String.valueOf(section.getId()))
                                    .sectionName(section.getName())
                                    .position(section.getPosition())
                                    .questions(questions)
                                    .build();
                        })
                .toList();
    }

    public List<SectionWithQuestionDto> getQuestionnaireWithSectionByIndustryIds(List<Long> industryIds) {
        List<QuestionDto> questionDtos = questionSearchService.searchQuestions(null, industryIds, Pageable.unpaged())
                .getContent().stream()
                .map(QuestionDto::toDto)
                .toList();

        Set<Long> sectionIds = questionDtos.stream()
                .map(QuestionDto::getQuestionSectionId)
                .collect(Collectors.toSet());

        Map<Long, QuestionSectionDto> sectionMap = questionSectionService
                .getQuestionSectionsByIds(sectionIds).stream()
                .collect(Collectors.toMap(QuestionSectionDto::getId, dto -> dto));

        Map<Long, List<QuestionDto>> questionsBySection = questionDtos.stream()
                .collect(Collectors.groupingBy(
                        QuestionDto::getQuestionSectionId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(
                                                QuestionDto::getPosition,
                                                Comparator.nullsLast(Comparator.naturalOrder())
                                        ))
                                        .toList()
                        )
                ));

        return questionsBySection.entrySet().stream()
                .map(entry -> {
                    QuestionSectionDto section = sectionMap.get(entry.getKey());
                    return SectionWithQuestionDto.builder()
                            .sectionId(String.valueOf(entry.getKey()))
                            .sectionName(section != null ? section.getName() : null)
                            .position(section != null ? section.getPosition() : null)
                            .questions(entry.getValue())
                            .build();
                })
                .toList();
    }

    public List<QuestionDto> getQuestionsByIndustryIds(List<Long> industryIds) {
        return questionRepository.findByIndustryIdsInAndActiveTrue(industryIds).stream()
                .map(QuestionDto::toDto)
                .toList();
    }

    public List<QuestionDto> getAllQuestionsByIndustryId(Long industryId) {
        return getQuestionsByIndustryIds(List.of(industryId));
    }

    public List<QuestionDto> bulkSaveQuestions(Long sectionId, List<QuestionDto> questionDtos) {
        questionSectionService.findQuestionSectionById(sectionId);

        return questionDtos.stream()
                .map(dto -> {
                    dto.setQuestionSectionId(sectionId);

                    return (dto.getId() != null)
                            ? updateQuestion(String.valueOf(dto.getId()), dto)
                            : createQuestion(dto);
                })
                .toList();
    }

    public QuestionDto getQuestionById(String questionId) {
        return QuestionDto.toDto(findQuestionById(questionId));
    }

    private Question findQuestionById(String id) {
        return questionRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    public List<QuestionDto> getQuestionsByIds(List<Long> ids) {
        return questionRepository.findAllById(ids)
                .stream()
                .map(QuestionDto::toDto)
                .toList();
    }

    public void validateQuestionIds(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds))
            return;
        long count = questionRepository.countAllByIdInAndActiveTrue(questionIds);
        if (count != questionIds.size()) {
            throw new BadRequestException("Question Ids are not valid");
        }
    }

    private void validateQuestionOptions(QuestionDto questionDto) {
        if (questionDto.getType() == QuestionType.MULTISELECT || questionDto.getType() == QuestionType.RADIO) {
            if (ObjectUtils.isEmpty(questionDto.getOptions())) {
                throw new BadRequestException("Question Type Must Have Options");
            }
        } else {
            if (!ObjectUtils.isEmpty(questionDto.getOptions())) {
                throw new IllegalArgumentException("Question Type Does Not Support Options");
            }
        }
    }

}
