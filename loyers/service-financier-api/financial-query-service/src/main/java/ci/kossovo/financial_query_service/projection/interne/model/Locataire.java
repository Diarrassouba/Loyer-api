package ci.kossovo.financial_query_service.projection.interne.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
public class Locataire {

  @Id private String id;
  private String nom;
  private String prenom;
  private String email;
  private String telephone;

  public String getNomComplet() {
    return nom + " " + prenom;
  }
}
