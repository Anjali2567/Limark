package ai.leadplus.api.v1.emailimages;

import ai.leadplus.application.emailimages.EmailImageDto;
import ai.leadplus.application.emailimages.EmailImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/email-images")
@RequiredArgsConstructor
@Tag(name = "Email Images", description = "APIs Email Images")
public class EmailImageController {

    private final EmailImageService emailImageService;

    @Operation(summary = "Create Email Image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmailImageResponse> createEmailImage(
            @ModelAttribute @Valid EmailImageRequest request) {

        log.info("[POST] Received request to create Email Image with file name: {}",
                request.getFile().getOriginalFilename());
        EmailImageDto emailImageDto =
                emailImageService.createEmailImage(request.toDto(), request.getFile());
        EmailImageResponse response = EmailImageResponse.toResponse(emailImageDto);
        log.info("[POST] Created Email Image with id: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
