package ci.kossovo.loyer_core_api.events.locations;

import java.math.BigDecimal;
import java.time.LocalDate;

// Événement publié lors de la création d'un contrat.
// Il contient toutes les infos dont le service financier a besoin.
public record ContratCreatedEvent(String contratId, String locataireId, String bienId, // ID de la maison ou de
                                                                                       // l'appartement
        String typeBien, // "MAISON" ou "APPARTEMENT"
        BigDecimal montantLoyerMensuel, LocalDate dateDebut) {

}
