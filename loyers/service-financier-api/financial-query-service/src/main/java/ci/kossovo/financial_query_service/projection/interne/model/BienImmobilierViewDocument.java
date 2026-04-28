package ci.kossovo.financial_query_service.projection.interne.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vue_biens_immobiliers")
public class BienImmobilierViewDocument {
  @Id private String bienId;

  // Le type précis ("VILLA", "STUDIO", etc.)
  private String typePrecis;

  // Si c'est un lot, on garde une trace de son bâtiment parent (ex: "dans l'Immeuble Le Concorde")
  private String typeBatimentParent;

  private String adresseComplete;
}
