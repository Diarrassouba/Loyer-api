package ci.kossovo.raiting_service.projections.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

// Table très simple pour stocker les IDs des contrats existants
@Entity
@Data
@NoArgsConstructor
public class ContratValideView {
    @Id
    private String contratId;
    private String locataireId; // Utile pour vérifier la cohérence

    public ContratValideView(String contratId, String locataireId) {
        this.contratId = contratId;
        this.locataireId = locataireId;
    }
}
