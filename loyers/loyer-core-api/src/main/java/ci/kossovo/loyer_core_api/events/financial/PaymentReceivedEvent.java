package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentReceivedEvent(
    String contratId,
    UUID paiementId,
    BigDecimal montantPaye,
    LocalDate datePaiement,
    BigDecimal nouveauSolde) {}
