package ci.kossovo.locataire_rest_api.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ContratLocation {

     @Id
    private String id = UUID.randomUUID().toString();
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal montantLoyerBase;
    private boolean actif = true;
    
    // Identifiants externes
    private String locataireId;
    private String appartementId; // Soit l'un...
    private String maisonId;      // ...soit l'autre

}
