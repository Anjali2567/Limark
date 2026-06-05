package ai.leadplus.application.attachments;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.domain.attachments.Attachment;
import ai.leadplus.domain.attachments.AttachmentRepository;
import ai.leadplus.domain.attachments.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttachmentServiceTest {

    private AttachmentRepository attachmentRepository;
    private AwsS3Service awsS3Service;
    private AttachmentService attachmentService;

    @BeforeEach
    void setup() {
        attachmentRepository = mock(AttachmentRepository.class);
        awsS3Service = mock(AwsS3Service.class);
        attachmentService = new AttachmentService(attachmentRepository, awsS3Service);
    }

    @Test
    void createAttachment_uploadsFile_andSaves_andReturnsDto() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("attachment-123.pdf");
        when(mockFile.getSize()).thenReturn(1024L);

        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(123L);
            }
            return arg;
        });

        when(awsS3Service.uploadFile(mockFile, "attachments/request-for-quotes", "123"))
                .thenReturn("https://s3.amazonaws.com/bucket/attachments/request-for-quotes/attachment-123.pdf");

        AttachmentDto result = attachmentService.createAttachment(1L, SourceType.REQUEST_FOR_QUOTE, mockFile);

        assertEquals(123L, result.getId());
        assertEquals(1L, result.getSourceId());
        assertEquals(SourceType.REQUEST_FOR_QUOTE, result.getSourceType());
        assertEquals("attachment-123", result.getFileName());
        assertEquals("pdf", result.getFileType());
        assertEquals("https://s3.amazonaws.com/bucket/attachments/request-for-quotes/attachment-123.pdf", result.getFileUrl());
        assertEquals(1024L, result.getSizeBytes());

        verify(attachmentRepository, times(2)).save(any(Attachment.class));
        verify(awsS3Service).uploadFile(mockFile, "attachments/request-for-quotes", "123");
    }

    @Test
    void createAttachment_throwsBadRequestException_whenFileIsNull() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> attachmentService.createAttachment(1L, SourceType.REQUEST_FOR_QUOTE, null)
        );

        assertEquals("File is empty", exception.getMessage());
        verify(attachmentRepository, never()).save(any(Attachment.class));
        verify(awsS3Service, never()).uploadFile(any(MultipartFile.class), anyString(), anyString());
    }

    @Test
    void createAttachment_throwsBadRequestException_whenFileIsEmpty() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> attachmentService.createAttachment(1L, SourceType.REQUEST_FOR_QUOTE, mockFile)
        );

        assertEquals("File is empty", exception.getMessage());
        verify(attachmentRepository, never()).save(any(Attachment.class));
        verify(awsS3Service, never()).uploadFile(any(MultipartFile.class), anyString(), anyString());
    }

    @Test
    void createAttachment_handlesFileWithoutExtension() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("attachment-456");
        when(mockFile.getSize()).thenReturn(2048L);

        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(456L);
            }
            return arg;
        });

        when(awsS3Service.uploadFile(mockFile, "attachments/request-for-proposals", "456"))
                .thenReturn("https://s3.amazonaws.com/bucket/attachments/attachment-456");

        AttachmentDto result = attachmentService.createAttachment(2L, SourceType.REQUEST_FOR_PROPOSAL, mockFile);

        assertEquals(456L, result.getId());
        assertEquals("attachment-456", result.getFileName());
        assertEquals("", result.getFileType()); // FileReader.getFileExtension returns "" for files without extension
        assertEquals(2048L, result.getSizeBytes());

        verify(attachmentRepository, times(2)).save(any(Attachment.class));
        verify(awsS3Service).uploadFile(mockFile, "attachments/request-for-proposals", "456");
    }

    @Test
    void getAttachments_returnsListOfDto_whenAttachmentsExist() {
        Attachment attachment1 = Attachment.builder()
                .id(1L)
                .sourceId(1L)
                .sourceType(SourceType.SHOWCASE)
                .fileName("file1")
                .fileType("pdf")
                .fileUrl("https://s3.amazonaws.com/bucket/attachments/att-1/file1.pdf")
                .sizeBytes(1024L)
                .active(true)
                .build();

        Attachment attachment2 = Attachment.builder()
                .id(2L)
                .sourceId(1L)
                .sourceType(SourceType.SHOWCASE)
                .fileName("file2")
                .fileType("docx")
                .fileUrl("https://s3.amazonaws.com/bucket/attachments/att-2/file2.docx")
                .sizeBytes(2048L)
                .active(true)
                .build();

        List<Attachment> attachments = Arrays.asList(attachment1, attachment2);

        when(attachmentRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(1L, SourceType.SHOWCASE))
                .thenReturn(attachments);

        List<AttachmentDto> result = attachmentService.getAttachments(1L, SourceType.SHOWCASE);

        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals("file1", result.get(0).getFileName());
        assertEquals("pdf", result.get(0).getFileType());
        assertEquals(2L, result.get(1).getId());
        assertEquals("file2", result.get(1).getFileName());
        assertEquals("docx", result.get(1).getFileType());

        verify(attachmentRepository).findAllBySourceIdAndSourceTypeAndActiveTrue(1L, SourceType.SHOWCASE);
    }

    @Test
    void getAttachments_returnsEmptyList_whenNoAttachmentsExist() {
        when(attachmentRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(999L, SourceType.QUOTATION))
                .thenReturn(List.of());

        List<AttachmentDto> result = attachmentService.getAttachments(999L, SourceType.QUOTATION);

        assertTrue(result.isEmpty());
        verify(attachmentRepository).findAllBySourceIdAndSourceTypeAndActiveTrue(999L, SourceType.QUOTATION);
    }


}
