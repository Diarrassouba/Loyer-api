package ci.kossovo.locataire_rest_api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratResponseDTO(
    String id,
    LocalDate dateDebut,
    LocalDate dateFin,
    BigDecimal montantLoyerBase,
    boolean actif,
    String locataireId,
    String bienId, // On unifie la réponse pour plus de simplicité
    String typeBien
) {

}
