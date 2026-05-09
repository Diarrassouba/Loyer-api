package ci.kossovo.locataire_rest_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class AppartementDispoView {
  @Id private String appartementId;

  // INFO PARENT DEMANDÉE
  private String maisonId;

  private boolean loue = false;

  public AppartementDispoView(String appartementId, String maisonId) {
    this.appartementId = appartementId;
    this.maisonId = maisonId;
  }
}
