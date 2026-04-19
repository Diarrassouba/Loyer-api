package ci.kossovo.financial_query_service.projection.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "transactions")
public class TransactionDocument {
  @Id private String transactionId;
  private String contratId;
  // private Contrat contrat; // Détails du contrat pour référence
  // private Locataire locataire; // Détails du locataire pour référence
  private LocalDateTime date;
  private String description;
  private String type; // "LOYER" ou "PAIEMENT"
  private BigDecimal montant;
  private BigDecimal soldeApresTransaction;
}
