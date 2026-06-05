package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.SocialMedia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaDto {

    private String linkedin;
    private String twitter;
    private String facebook;
    private String instagram;
    private String youtube;

    public static SocialMediaDto fromEntity(SocialMedia socialMedia) {
        if (socialMedia == null) return null;
        return SocialMediaDto.builder()
                .linkedin(socialMedia.getLinkedin())
                .twitter(socialMedia.getTwitter())
                .facebook(socialMedia.getFacebook())
                .instagram(socialMedia.getInstagram())
                .youtube(socialMedia.getYoutube())
                .build();
    }

    public SocialMedia toEntity() {
        return SocialMedia.builder()
                .linkedin(linkedin)
                .twitter(twitter)
                .facebook(facebook)
                .instagram(instagram)
                .youtube(youtube)
                .build();
    }
}
