package ai.leadplus.application.industries;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.industries.Industry;
import ai.leadplus.domain.industries.IndustryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndustryServiceTest {

    private IndustryRepository industryRepository;
    private AwsS3Service awsS3Service;
    private IndustryService industryService;

    @BeforeEach
    void setup() {
        industryRepository = mock(IndustryRepository.class);
        awsS3Service = mock(AwsS3Service.class);
        industryService = new IndustryService(industryRepository, awsS3Service);
    }

    @Test
    void createIndustry_setsActiveTrue_andSaves_andReturnsDto() {
        IndustryDto input = IndustryDto.builder()
                .name("Tech")
                .slug("tech")
                .description("Tech desc")
                .disabled(false)
                .active(false)
                .build();

        Industry saved = Industry.builder()
                .id(1L)
                .name("Tech")
                .slug("tech")
                .description("Tech desc")
                .disabled(false)
                .active(true)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedBy(1L)
                .updatedAt(LocalDateTime.now())
                .build();

        when(industryRepository.existsByNameIgnoreCaseAndActiveTrue("Tech")).thenReturn(false);
        when(industryRepository.save(any(Industry.class))).thenReturn(saved);

        IndustryDto result = industryService.createIndustry(input);

        verify(industryRepository).existsBySlugIgnoreCaseAndActiveTrue("tech");
        ArgumentCaptor<Industry> captor = ArgumentCaptor.forClass(Industry.class);
        verify(industryRepository).save(captor.capture());
        Industry toSave = captor.getValue();
        assertTrue(toSave.isActive(), "createIndustry should force active=true");

        assertEquals(1L, result.getId());
        assertEquals("Tech", result.getName());
        assertTrue(result.isActive());
        assertFalse(result.isDisabled());
    }

    @Test
    void createIndustry_throwsBadRequestException_whenNameAlreadyExists() {
        IndustryDto input = IndustryDto.builder()
                .name("Tech")
                .slug("tech")
                .description("Tech desc")
                .disabled(false)
                .active(false)
                .build();

        when(industryRepository.existsBySlugIgnoreCaseAndActiveTrue("tech")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> industryService.createIndustry(input)
        );

        assertEquals("Duplicate industry: " + input.getName(), exception.getMessage());
        verify(industryRepository).existsBySlugIgnoreCaseAndActiveTrue("tech");
        verify(industryRepository, never()).save(any(Industry.class));
    }

    @Test
    void getIndustryById_returnsDto_whenFoundAndActive() {
        Industry found = Industry.builder()
                .id(1L)
                .name("Finance")
                .description("Finance desc")
                .active(true)
                .disabled(false)
                .build();

        when(industryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(found));

        IndustryDto dto = industryService.getIndustryById("1");

        assertEquals(1L, dto.getId());
        assertEquals("Finance", dto.getName());
    }

    @Test
    void getIndustryById_throws_whenNotFoundOrInactive() {
        when(industryRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> industryService.getIndustryById("2"));
    }

    @Test
    void updateIndustry_updatesNameAndDescription_andReturnsDto() {
        Industry existing = Industry.builder()
                .id(2L)
                .name("Old")
                .slug("slug")
                .description("Old desc")
                .active(true)
                .disabled(false)
                .build();

        IndustryDto updateDto = IndustryDto.builder()
                .name("New")
                .slug("slug")
                .description("New desc")
                .build();

        Industry updated = Industry.builder()
                .id(2L)
                .name("New")
                .slug("slug")
                .description("New desc")
                .active(true)
                .disabled(false)
                .build();

        when(industryRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(existing));
        when(industryRepository.save(any(Industry.class))).thenReturn(updated);

        IndustryDto result = industryService.updateIndustry("2", updateDto);

        assertEquals(2L, result.getId());
        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());

        ArgumentCaptor<Industry> captor = ArgumentCaptor.forClass(Industry.class);
        verify(industryRepository).save(captor.capture());
        Industry saved = captor.getValue();
        assertEquals("New", saved.getName());
        assertEquals("New desc", saved.getDescription());
    }

    @Test
    void updateIndustry_throws_whenNotFound() {
        when(industryRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());
        IndustryDto updateDto = IndustryDto.builder().name("X").description("Y").build();
        assertThrows(ResourceNotFoundException.class, () -> industryService.updateIndustry("404", updateDto));
    }

    @Test
    void updateDisableStatus_noChange_returnsExistingDto_withoutSave() {
        Industry existing = Industry.builder()
                .id(3L)
                .name("Retail")
                .description("Retail desc")
                .active(true)
                .disabled(true)
                .build();

        when(industryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(existing));

        IndustryDto result = industryService.updateDisableStatus("3", true);

        assertTrue(result.isDisabled());
        verify(industryRepository, never()).save(any());
    }

    @Test
    void updateDisableStatus_changesFlag_andSaves_andReturnsDto() {
        Industry existing = Industry.builder()
                .id(4L)
                .name("Energy")
                .description("Energy desc")
                .active(true)
                .disabled(false)
                .build();

        when(industryRepository.findByIdAndActiveTrue(4L)).thenReturn(Optional.of(existing));
        when(industryRepository.save(any(Industry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IndustryDto result = industryService.updateDisableStatus("4", true);

        assertTrue(result.isDisabled());

        ArgumentCaptor<Industry> captor = ArgumentCaptor.forClass(Industry.class);
        verify(industryRepository).save(captor.capture());
        assertTrue(captor.getValue().isDisabled());
    }

    @Test
    void updateDisableStatus_throws_whenNotFound() {
        when(industryRepository.findByIdAndActiveTrue(405L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> industryService.updateDisableStatus("405", true));
    }

    @Test
    void getAllIndustries_returnsPageOfDto() {
        Industry industry1 = Industry.builder()
                .id(1L)
                .name("Tech")
                .description("Tech desc")
                .active(true)
                .disabled(false)
                .build();

        Industry industry2 = Industry.builder()
                .id(2L)
                .name("Finance")
                .description("Finance desc")
                .active(true)
                .disabled(false)
                .build();

        List<Industry> industries = Arrays.asList(industry1, industry2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Industry> industryPage = new PageImpl<>(industries, pageable, industries.size());

        when(industryRepository.findAllByDisabledFalseAndActiveTrue(pageable)).thenReturn(industryPage);

        Page<IndustryDto> result = industryService.getAllIndustries("", pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Tech", result.getContent().get(0).getName());
        assertEquals("Finance", result.getContent().get(1).getName());
        verify(industryRepository).findAllByDisabledFalseAndActiveTrue(pageable);
    }

    @Test
    void getAllIndustries_returnsEmptyPage_whenNoIndustries() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Industry> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(industryRepository.findAllByDisabledFalseAndActiveTrue(pageable)).thenReturn(emptyPage);

        Page<IndustryDto> result = industryService.getAllIndustries("", pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(industryRepository).findAllByDisabledFalseAndActiveTrue(pageable);
    }

    @Test
    void uploadIndustryImageById_uploadsImage_andUpdatesIndustry_andReturnsDto() {
        Industry existing = Industry.builder()
                .id(5L)
                .name("Healthcare")
                .description("Healthcare desc")
                .active(true)
                .disabled(false)
                .build();

        Industry updated = Industry.builder()
                .id(5L)
                .name("Healthcare")
                .description("Healthcare desc")
                .image("https://s3.amazonaws.com/bucket/industries/id-5/image.jpg")
                .active(true)
                .disabled(false)
                .build();

        MultipartFile mockFile = mock(MultipartFile.class);

        when(industryRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(existing));
        when(awsS3Service.uploadFile(mockFile, "industries", "5"))
                .thenReturn("https://s3.amazonaws.com/bucket/industries/id-5/image.jpg");
        when(industryRepository.save(any(Industry.class))).thenReturn(updated);

        IndustryDto result = industryService.uploadIndustryImageById("5", mockFile);

        assertEquals(5L, result.getId());
        assertEquals("https://s3.amazonaws.com/bucket/industries/id-5/image.jpg", result.getImage());

        verify(industryRepository).findByIdAndActiveTrue(5L);
        verify(awsS3Service).uploadFile(mockFile, "industries", "5");
        ArgumentCaptor<Industry> captor = ArgumentCaptor.forClass(Industry.class);
        verify(industryRepository).save(captor.capture());
        assertEquals("https://s3.amazonaws.com/bucket/industries/id-5/image.jpg", captor.getValue().getImage());
    }

    @Test
    void uploadIndustryImageById_throws_whenIndustryNotFound() {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(industryRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> industryService.uploadIndustryImageById("404", mockFile));

        verify(industryRepository).findByIdAndActiveTrue(404L);
        verify(awsS3Service, never()).uploadFile(any(MultipartFile.class), anyString(), anyString());
        verify(industryRepository, never()).save(any(Industry.class));
    }
}
