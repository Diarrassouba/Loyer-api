package ci.kossovo.financial_query_service.projection.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Dans SyntheseFinanciereDocument.java
@Document(collection = "syntheses_financieres")
@Data
@NoArgsConstructor
public class SyntheseFinanciereDocument {
  @Id private String contratId;
 // private Contrat contrat; // Détails du contrat pour référence
  private String locataireId;
   private String bienId;
  ///private Locataire locataire;
  private BigDecimal montantLoyerMensuel;
  private BigDecimal solde;
  private String dernierMoisGenere; // Stocké en String (ex: "2023-10") pour simplifier MongoDB

 
   public SyntheseFinanciereDocument(String contratId, String locataireId, String bienId) {
        this.contratId = contratId;
        this.locataireId = locataireId;
        this.bienId = bienId;
        this.solde = BigDecimal.ZERO;
    }
}
