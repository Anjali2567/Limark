package ai.leadplus.application.attachmentlibraries;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.attachmentlibraries.AttachmentLibrary;
import ai.leadplus.domain.attachmentlibraries.AttachmentLibraryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentLibraryServiceTest {

    @Mock
    private AttachmentLibraryRepository attachmentLibraryRepository;

    @Mock
    private AttachmentLibrarySearchService attachmentLibrarySearchService;

    @Mock
    private AwsS3Service awsS3Service;

    @InjectMocks
    private AttachmentLibraryService attachmentLibraryService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void searchResources_shouldReturnMappedDtos() {
        AttachmentLibrary resource = AttachmentLibrary.builder()
                .id(1L)
                .filename("file.pdf")
                .fileUrl("http://url")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<AttachmentLibrary> page =
                new PageImpl<>(List.of(resource), pageable, 1);

        when(attachmentLibrarySearchService
                .searchAttachmentLibraries("123", "file", List.of("SVG"), pageable))
                .thenReturn(page);

        Page<AttachmentLibraryDto> result =
                attachmentLibraryService.searchResources("123", "file", List.of("SVG"), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().getId());
        assertEquals("file.pdf", result.getContent().getFirst().getFilename());

        verify(attachmentLibrarySearchService)
                .searchAttachmentLibraries("123", "file", List.of("SVG"), pageable);
    }

    @Test
    void getResourceById_shouldReturnDto_whenExists() {
        AttachmentLibrary resource = AttachmentLibrary.builder()
                .id(1L)
                .filename("test.pdf")
                .build();

        when(attachmentLibraryRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        AttachmentLibraryDto result = attachmentLibraryService.getResourceById("1");

        assertEquals(1L, result.getId());
        assertEquals("test.pdf", result.getFilename());

        verify(attachmentLibraryRepository).findById(1L);
    }

    @Test
    void getResourceById_shouldThrow_whenNotFound() {
        when(attachmentLibraryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attachmentLibraryService.getResourceById("1"));

        verify(attachmentLibraryRepository).findById(1L);
    }

    @Test
    void createResource_shouldSaveResourceAndUploadToS3() {
        AttachmentLibrary saved = AttachmentLibrary.builder()
                .id(123L)
                .filename("doc.pdf")
                .fileType("pdf")
                .build();

        AttachmentLibrary savedWithUrl = AttachmentLibrary.builder()
                .id(123L)
                .filename("doc.pdf")
                .fileType("pdf")
                .fileUrl("https://s3.com/attachment-Libraries/123")
                .build();

        when(multipartFile.getOriginalFilename()).thenReturn("doc.pdf");

        when(attachmentLibraryRepository.save(any(AttachmentLibrary.class)))
                .thenReturn(saved);

        when(awsS3Service.uploadFile(multipartFile, "attachment-libraries", "123"))
                .thenReturn("https://s3.com/attachment-libraries/123");

        when(attachmentLibraryRepository.save(saved))
                .thenReturn(savedWithUrl);

        AttachmentLibraryDto result =
                attachmentLibraryService.createResource("123", multipartFile);

        assertEquals(123L, result.getId());
        assertEquals("doc.pdf", result.getFilename());
        assertEquals("https://s3.com/attachment-Libraries/123", result.getFileUrl());

        verify(multipartFile).getOriginalFilename();
        verify(attachmentLibraryRepository, times(2)).save(any());
        verify(awsS3Service).uploadFile(multipartFile, "attachment-libraries", "123");
    }
}
