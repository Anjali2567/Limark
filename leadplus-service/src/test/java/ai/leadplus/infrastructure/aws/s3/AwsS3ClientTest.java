package ai.leadplus.infrastructure.aws.s3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;


import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsS3ClientTest {

    @Mock
    private S3Template s3Template;

    @InjectMocks
    private AwsS3Client awsS3Client;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(awsS3Client, "bucketName", bucketName);
    }

    @Test
    void uploadObject_MultipartFile_shouldReturnS3Path() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        var mockResource = mock(S3Resource.class);
        when(mockResource.getURL()).thenReturn(new URL("http://s3/test.txt"));
        when(s3Template.upload(eq(bucketName), anyString(), any(InputStream.class))).thenReturn(mockResource);

        String path = awsS3Client.uploadObject(file, "folder", "key123");

        assertThat(path).isEqualTo("/assets/uploads/folder/key123.txt");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Template).upload(eq(bucketName), keyCaptor.capture(), any(InputStream.class));
        assertThat(keyCaptor.getValue()).isEqualTo("assets/uploads/folder/key123.txt");
    }

    @Test
    void uploadObject_File_shouldReturnS3Path() throws Exception {
        File tempFile = File.createTempFile("testFile", ".txt");
        tempFile.deleteOnExit();

        var mockResource = mock(S3Resource.class);
        when(mockResource.getURL()).thenReturn(new URL("http://s3/testFile.txt"));
        when(s3Template.upload(eq(bucketName), anyString(), any(InputStream.class))).thenReturn(mockResource);

        String path = awsS3Client.uploadObject(tempFile, "folder", "key123");

        assertThat(path).isEqualTo("/assets/uploads/folder/key123.txt");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Template).upload(eq(bucketName), keyCaptor.capture(), any(InputStream.class));
        assertThat(keyCaptor.getValue()).isEqualTo("assets/uploads/folder/key123.txt");
    }

    @Test
    void deleteObject_shouldCallS3TemplateDelete() {
        String s3Key = "/assets/uploads/folder/key123.txt";

        awsS3Client.deleteObject(s3Key);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Template).deleteObject(eq(bucketName), keyCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo("assets/uploads/folder/key123.txt");
    }

    @Test
    void deleteObject_nullOrEmpty_shouldNotCallDelete() {
        awsS3Client.deleteObject(null);
        awsS3Client.deleteObject("");

        verifyNoInteractions(s3Template);
    }

    @Test
    void getObject_shouldReturnS3Resource(){
        String s3Key = "/assets/uploads/folder/key123.txt";
        S3Resource mockResource = mock(S3Resource.class);
        when(s3Template.download(bucketName, "assets/uploads/folder/key123.txt")).thenReturn(mockResource);

        S3Resource result = awsS3Client.getObject(s3Key);

        assertThat(result).isEqualTo(mockResource);
        verify(s3Template).download(bucketName, "assets/uploads/folder/key123.txt");
    }

    @Test
    void getObject_nullOrEmpty_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> awsS3Client.getObject(null));
        assertThrows(IllegalArgumentException.class, () -> awsS3Client.getObject(""));
    }
}
