package ai.leadplus.application.leadcontactnormalizedtitle;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactAiService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.TitleNormalizationResult;
import ai.leadplus.domain.leadcontactnormalizedtitle.LeadContactNormalizedTitle;
import ai.leadplus.domain.leadcontactnormalizedtitle.LeadContactNormalizedTitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LeadContactNormalizedTitleServiceTest {

    @Mock
    private LeadContactNormalizedTitleRepository repository;

    @Mock
    private LeadContactAiService aiService;

    @InjectMocks
    private LeadContactNormalizedTitleService service;

    private LeadContactDto dto;
    private TitleNormalizationResult result;

    @BeforeEach
    void setup() {
        dto = new LeadContactDto();
        dto.setId(1L);
        dto.setTitle("Software Engineer");

        result = new TitleNormalizationResult();
        result.setCanonicalTitle("Software Engineer");
        result.setSeniority("Mid");
        result.setNormalizedTitles(Collections.singletonList("software engineer"));
        result.setKeywords(Collections.singletonList("engineer"));
        result.setTitleAbbreviations(Collections.emptyList());
    }


    @Test
    void shouldCreateNewNormalizedTitle_whenNotExists() {
        when(repository.findByLeadContactId(dto.getId())).thenReturn(Optional.empty());
        when(aiService.normalizeTitle(dto.getTitle())).thenReturn(result);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadContactNormalizedTitleDto response =
                service.createOrUpdateLeadContactNormalizedTitle(dto);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getCanonicalTitle());

        verify(aiService, times(1)).normalizeTitle(dto.getTitle());
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldUpdate_whenTitleChanged() {
        LeadContactNormalizedTitle existing = LeadContactNormalizedTitle.builder()
                .leadContactId(dto.getId())
                .originalTitle("Developer")
                .build();

        when(repository.findByLeadContactId(dto.getId())).thenReturn(Optional.of(existing));
        when(aiService.normalizeTitle(dto.getTitle())).thenReturn(result);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadContactNormalizedTitleDto response =
                service.createOrUpdateLeadContactNormalizedTitle(dto);

        assertEquals("Software Engineer", response.getOriginalTitle());
        verify(aiService, times(1)).normalizeTitle(dto.getTitle());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void shouldSkipUpdate_whenTitleSame() {
        LeadContactNormalizedTitle existing = LeadContactNormalizedTitle.builder()
                .leadContactId(dto.getId())
                .originalTitle("Software Engineer")
                .build();

        when(repository.findByLeadContactId(dto.getId())).thenReturn(Optional.of(existing));

        LeadContactNormalizedTitleDto response =
                service.createOrUpdateLeadContactNormalizedTitle(dto);

        assertEquals("Software Engineer", response.getOriginalTitle());

        verify(aiService, never()).normalizeTitle(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnNormalizedTitle_whenExists() {
        LeadContactNormalizedTitle entity = LeadContactNormalizedTitle.builder()
                .leadContactId(1L)
                .originalTitle("Software Engineer")
                .build();

        when(repository.findByLeadContactId(1L))
                .thenReturn(Optional.of(entity));

        LeadContactNormalizedTitleDto response =
                service.getLeadContactNormalizedTitle(1L);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getOriginalTitle());
    }

    @Test
    void shouldThrowException_whenNotFound() {
        when(repository.findByLeadContactId(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.getLeadContactNormalizedTitle(1L));
    }

    @Test
    void shouldThrowException_whenDtoIsNull() {
        assertThrows(BadRequestException.class, () ->
                service.createOrUpdateLeadContactNormalizedTitle(null));
    }

    @Test
    void shouldThrowException_whenIdIsNull() {
        LeadContactDto invalidDto = new LeadContactDto();
        invalidDto.setTitle("Engineer");

        assertThrows(BadRequestException.class, () ->
                service.createOrUpdateLeadContactNormalizedTitle(invalidDto));
    }
}
