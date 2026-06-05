package ai.leadplus.application.promptspecifications;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.promptspecifications.PromptSpecification;
import ai.leadplus.domain.promptspecifications.PromptSpecificationRepository;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptSpecificationServiceTest {

    @Mock
    private PromptSpecificationRepository promptSpecificationRepository;

    @InjectMocks
    private PromptSpecificationService promptSpecificationService;

    private PromptSpecification promptSpecification;

    @BeforeEach
    void setUp() {
        promptSpecification = PromptSpecification.builder()
                .id(123L)
                .type(PromptSpecificationType.CAMPAIGN_COPILOT)
                .promptTemplate("Original Prompt")
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getLatestPromptSpecificationByType_ShouldReturnDto_WhenFound() {
        when(promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(PromptSpecificationType.CAMPAIGN_COPILOT))
                .thenReturn(promptSpecification);

        var result = promptSpecificationService.getLatestPromptSpecificationByType(PromptSpecificationType.CAMPAIGN_COPILOT);

        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getPromptTemplate()).isEqualTo("Original Prompt");
        verify(promptSpecificationRepository, times(1))
                .findFirstByTypeOrderByCreatedAtDesc(PromptSpecificationType.CAMPAIGN_COPILOT);
    }

    @Test
    void getLatestPromptSpecificationByType_ShouldThrowException_WhenNotFound() {
        when(promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(PromptSpecificationType.CAMPAIGN_COPILOT))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> promptSpecificationService.getLatestPromptSpecificationByType(PromptSpecificationType.CAMPAIGN_COPILOT));

        verify(promptSpecificationRepository, times(1))
                .findFirstByTypeOrderByCreatedAtDesc(PromptSpecificationType.CAMPAIGN_COPILOT);
    }

    @Test
    void updatePromptSpecification_ShouldUpdateAndReturnDto_WhenFound() {
        PromptSpecificationDto updateDto = PromptSpecificationDto.builder()
                .id(123L)
                .type(PromptSpecificationType.CAMPAIGN_COPILOT)
                .promptTemplate("Updated Prompt")
                .createdBy(2L)
                .build();

        when(promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(updateDto.getType()))
                .thenReturn(promptSpecification);
        when(promptSpecificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = promptSpecificationService.updatePromptSpecification(updateDto);

        assertThat(result.getPromptTemplate()).isEqualTo("Updated Prompt");
        assertThat(result.getCreatedBy()).isEqualTo(2L);

        verify(promptSpecificationRepository, times(1))
                .save(promptSpecification);
    }

    @Test
    void updatePromptSpecification_ShouldThrowException_WhenNotFound() {
        PromptSpecificationDto dto = PromptSpecificationDto.builder()
                .type(PromptSpecificationType.CAMPAIGN_COPILOT)
                .build();

        when(promptSpecificationRepository.findFirstByTypeOrderByCreatedAtDesc(dto.getType()))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                promptSpecificationService.updatePromptSpecification(dto)
        );

        verify(promptSpecificationRepository, times(1))
                .findFirstByTypeOrderByCreatedAtDesc(dto.getType());
    }
}
