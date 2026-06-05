package ai.leadplus.application.emailimages;

import ai.leadplus.application.aws.AwsS3Service;
import ai.leadplus.domain.emailimages.EmailImage;
import ai.leadplus.domain.emailimages.EmailImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EmailImageService {

    private final EmailImageRepository emailImageRepository;
    private final AwsS3Service awsS3Service;

    public EmailImageDto createEmailImage(EmailImageDto emailImageDto, MultipartFile file) {
        EmailImage savedResource = emailImageRepository.save(emailImageDto.toEntity());
        String resourceUrl = awsS3Service.uploadFile(file, "email-images", String.valueOf(savedResource.getId()));
        savedResource.setResourceUrl(resourceUrl);
        return EmailImageDto.fromEntity(emailImageRepository.save(savedResource));
    }
}
