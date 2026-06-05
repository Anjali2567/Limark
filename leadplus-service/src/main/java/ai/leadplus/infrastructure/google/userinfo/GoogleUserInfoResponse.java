package ai.leadplus.infrastructure.google.userinfo;

import lombok.Data;

@Data
public class GoogleUserInfoResponse {
    private String id;
    private String email;
    private String verified_email;
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
}
