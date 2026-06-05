package ai.leadplus.domain.vendoragreement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Otp {
    private String otpCode;
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
    private int attempt;
}
