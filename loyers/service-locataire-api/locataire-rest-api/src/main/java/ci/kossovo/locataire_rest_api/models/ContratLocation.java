package ci.kossovo.locataire_rest_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Entity
@Data
public class ContratLocation {

  @Id private String id = UUID.randomUUID().toString();

  // Référence au locataire
  private String locataireId;

  // --- Gestion de la hiérarchie du bien ---
  // En base de données, on stocke explicitement si c'est une maison ou un appartement.
  // L'un des deux sera toujours null.
  private String maisonId;
  private String appartementId;

  // Utile pour des requêtes simples (ex: "MAISON" ou "APPARTEMENT")
  private String typeBien;

  // --- Détails financiers et temporels ---
  private BigDecimal montantLoyerBase;
  private LocalDate dateDebut;
  private LocalDate dateFin;

  // Indique si le contrat est en cours
  private boolean actif = true;

  // Dans ContratLocation.java
  private BigDecimal montantCaution; // Stockera la valeur de 2 mois de loyer
  private BigDecimal montantAvance; // Stockera la valeur de l'avance (minimum 1 mois)
}
