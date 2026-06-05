package ai.leadplus.application.emailimages;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.domain.emailimages.EmailImage;
import ai.leadplus.domain.emailimages.EmailImageRepository;
import ai.leadplus.domain.emailimages.EmailImageSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailImageServiceTest {

    @Mock
    private EmailImageRepository emailImageRepository;

    @Mock
    private AwsS3Service awsS3Service;

    @InjectMocks
    private EmailImageService emailImageService;

    private EmailImageDto emailImageDto;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        emailImageDto = EmailImageDto.builder()
                .sourceId(1L)
                .sourceType(EmailImageSourceType.DIRECT)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();

        file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "test-content".getBytes()
        );
    }

    @Test
    void createEmailImage_ShouldSaveEntity_UploadFile_AndReturnUpdatedDto() {

        EmailImage savedEntity = EmailImage.builder()
                .id(1L)
                .sourceId(emailImageDto.getSourceId())
                .sourceType(emailImageDto.getSourceType())
                .createdBy(emailImageDto.getCreatedBy())
                .createdAt(emailImageDto.getCreatedAt())
                .build();

        EmailImage updatedEntity = EmailImage.builder()
                .id(1L)
                .sourceId(emailImageDto.getSourceId())
                .sourceType(emailImageDto.getSourceType())
                .resourceUrl("https://s3.aws.com/email-images/image-id-1")
                .createdBy(emailImageDto.getCreatedBy())
                .createdAt(emailImageDto.getCreatedAt())
                .build();

        when(emailImageRepository.save(any(EmailImage.class)))
                .thenReturn(savedEntity)
                .thenReturn(updatedEntity);

        when(awsS3Service.uploadFile(eq(file), eq("email-images"), eq("1")))
                .thenReturn("https://s3.aws.com/email-images/image-id-1");

        EmailImageDto result =
                emailImageService.createEmailImage(emailImageDto, file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getResourceUrl())
                .isEqualTo("https://s3.aws.com/email-images/image-id-1");
        assertThat(result.getSourceType())
                .isEqualTo(EmailImageSourceType.DIRECT);

        verify(emailImageRepository, times(2)).save(any(EmailImage.class));
        verify(awsS3Service, times(1))
                .uploadFile(file, "email-images", "1");
    }
}
