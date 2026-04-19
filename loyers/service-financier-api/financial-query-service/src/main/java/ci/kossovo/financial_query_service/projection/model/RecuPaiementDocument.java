package ci.kossovo.financial_query_service.projection.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "recus_paiement")
public class RecuPaiementDocument {
  @Id private String paiementId;
  private String contratId;
  // private Contrat contrat; // Détails du contrat pour référence
  private String locataireId;
  // private Locataire locataire; // Détails du locataire pour référence
  private LocalDate datePaiement;
  private BigDecimal montantPaye;
  private BigDecimal soldeAvantPaiement;
  private BigDecimal soldeApresPaiement;
  private String statut;
}
