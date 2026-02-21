package ci.kossovo.financial_command_service.projection.models;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cette vue est une projection locale utilisée UNIQUEMENT par les processus internes
 * du service de commande (ex: le scheduler de génération de loyer).
 * Elle n'est pas exposée via une API.
 */

@Entity
@Data
@NoArgsConstructor
public class ContratActifView {

    @Id
    private String contratId;
    private BigDecimal montantLoyer;
    private boolean actif = true;

    public ContratActifView(String contratId, BigDecimal montantLoyer) {
        this.contratId = contratId;
        this.montantLoyer = montantLoyer;
    }
}
