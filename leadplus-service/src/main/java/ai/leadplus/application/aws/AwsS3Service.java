package ai.leadplus.application.aws;

import ai.leadplus.infrastructure.aws.s3.AwsS3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {
    private final AwsS3Client awsS3Client;

    public String uploadFile(MultipartFile file, String folder, String fileName) {
        return awsS3Client.uploadObject(file, folder, fileName);
    }

    public String uploadFile(File file, String folder, String fileName) {
        return awsS3Client.uploadObject(file, folder, fileName);
    }

    public void deleteExistingFile(String fileUrl) {
        awsS3Client.deleteObject(fileUrl);
    }
}