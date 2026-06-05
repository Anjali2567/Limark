package ai.leadplus.infrastructure.aws.s3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Client {

    private final S3Template s3Template;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @SneakyThrows
    public String uploadObject(MultipartFile file, String folder, String key) {
        String fileExtension = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String s3Key = "assets/uploads/" + folder + "/" + key + fileExtension;
        s3Template.upload(bucketName, s3Key, file.getInputStream()).getURL();
        log.info("Successfully uploaded to S3 '{}'", s3Key);
        return "/" + s3Key;
    }

    @SneakyThrows
    public String uploadObject(File file, String folder, String key) {
        String originalFilename = file.getName();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String s3Key = "assets/uploads/" + folder + "/" + key + fileExtension;
        InputStream inputStream = new FileInputStream(file);
        s3Template.upload(bucketName, s3Key, inputStream).getURL();
        log.info("Successfully uploaded to S3 '{}'", s3Key);
        return "/" + s3Key;
    }

    public void deleteObject(String s3Key) {
        if (s3Key != null && !s3Key.isEmpty()) {
            s3Key = s3Key.startsWith("/") ? s3Key.substring(1) : s3Key;
            s3Template.deleteObject(bucketName, s3Key);
            log.info("Successfully deleted from S3 '{}'", s3Key);
        }
    }

    @SneakyThrows
    public S3Resource getObject(String s3Key) {
        if (s3Key != null && !s3Key.isEmpty()) {
            s3Key = s3Key.startsWith("/") ? s3Key.substring(1) : s3Key;
            log.info("Retrieving object from S3 '{}'", s3Key);
            return s3Template.download(bucketName, s3Key);
        }
        throw new IllegalArgumentException("S3 key cannot be null or empty");
    }
}