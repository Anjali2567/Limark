package ai.leadplus.api.v1.leadcontacts;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactWithDomainRequest extends LeadContactRequest {
  @NotBlank(message = "Domain is mandatory")
  private String domain;
}
