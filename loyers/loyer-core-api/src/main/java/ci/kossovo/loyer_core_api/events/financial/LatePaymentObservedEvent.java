package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.YearMonth;

public record LatePaymentObservedEvent(
    String contratId,
    String locataireId, // Ajout crucial pour notre projection
    YearMonth moisAnnee,
    BigDecimal montantManquant) {}
