package ai.leadplus.application.questionsection;

import ai.leadplus.application.exception.DuplicateResourceException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.questionsection.QuestionSection;
import ai.leadplus.domain.questionsection.QuestionSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionSectionService {

    private final QuestionSectionRepository questionSectionRepository;

    public List<QuestionSectionDto> getAllQuestionSections() {
        return questionSectionRepository.findAllByActiveTrue().stream()
                .map(QuestionSectionDto::toDto)
                .toList();
    }

    public List<QuestionSectionDto> getQuestionSectionsByIds(Set<Long> ids) {
        List<QuestionSection> sections = questionSectionRepository.findAllByIdInAndActiveTrue(ids);
        return sections.stream()
                .map(QuestionSectionDto::toDto)
                .toList();
    }

    public QuestionSectionDto getQuestionSectionById(Long id) {
        QuestionSection section = findQuestionSectionById(id);
        return QuestionSectionDto.toDto(section);
    }

    public QuestionSection findQuestionSectionById(Long id) {
        return questionSectionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question section not found with id: " + id));
    }

    public QuestionSectionDto createQuestionSection(QuestionSectionDto questionSectionDto) {
        QuestionSection section = questionSectionDto.toEntity();
        section.setActive(true);
        if (questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue(section.getName())) {
            throw new DuplicateResourceException("Question section with name '" + section.getName() + "' already exists.");
        }
        if (section.getPosition() == null) {
            int nextPosition = questionSectionRepository.findAllByActiveTrue().stream()
                    .map(QuestionSection::getPosition)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            section.setPosition(nextPosition);
        }
        QuestionSection savedSection = questionSectionRepository.save(section);
        return QuestionSectionDto.toDto(savedSection);
    }

    public QuestionSectionDto updateQuestionSection(Long id, QuestionSectionDto questionSectionDto) {
        QuestionSection existingSection = findQuestionSectionById(id);
        if (!existingSection.getName().equalsIgnoreCase(questionSectionDto.getName()) &&
                questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue(questionSectionDto.getName())) {
            throw new DuplicateResourceException("Question section with name '" + questionSectionDto.getName() + "' already exists.");
        }
        existingSection.setName(questionSectionDto.getName());
        existingSection.setPosition(questionSectionDto.getPosition());
        QuestionSection savedSection = questionSectionRepository.save(existingSection);
        return QuestionSectionDto.toDto(savedSection);

    }

    public List<QuestionSectionDto> bulkSaveQuestionSections(List<QuestionSectionDto> dtos) {

        Set<Long> idsToUpdate = dtos.stream()
                .map(QuestionSectionDto::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        List<QuestionSection> existingSections = idsToUpdate.isEmpty()
                ? List.of()
                : questionSectionRepository.findAllByIdInAndActiveTrue(idsToUpdate);

        Map<Long, QuestionSection> existingMap = existingSections.stream()
                .collect(java.util.stream.Collectors.toMap(QuestionSection::getId, s -> s));

        if (existingSections.size() != idsToUpdate.size()) {
            throw new ResourceNotFoundException("Some QuestionSections not found for update");
        }

        List<QuestionSection> entitiesToSave = dtos.stream()
                .map(dto -> {

                    if (dto.getId() != null) {
                        QuestionSection existing = existingMap.get(dto.getId());

                        if (!existing.getName().equalsIgnoreCase(dto.getName())
                                && questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue(dto.getName())) {
                            throw new DuplicateResourceException(
                                    "Question section with name '" + dto.getName() + "' already exists."
                            );
                        }

                        existing.setName(dto.getName());
                        existing.setPosition(dto.getPosition());
                        return existing;

                    } else {
                        if (questionSectionRepository.existsByNameIgnoreCaseAndActiveTrue(dto.getName())) {
                            throw new DuplicateResourceException(
                                    "Question section with name '" + dto.getName() + "' already exists."
                            );
                        }

                        QuestionSection newEntity = dto.toEntity();
                        newEntity.setActive(true);
                        return newEntity;
                    }

                })
                .toList();
        List<QuestionSection> saved = questionSectionRepository.saveAll(entitiesToSave);

        return saved.stream()
                .map(QuestionSectionDto::toDto)
                .toList();
    }
}
