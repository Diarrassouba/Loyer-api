package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentReceivedEvent(
    String contratId,
     String locataireId, // Ajout crucial pour notre projection
    UUID paiementId,
    BigDecimal montantPaye,
    LocalDate datePaiement,
    BigDecimal nouveauSolde) {}
