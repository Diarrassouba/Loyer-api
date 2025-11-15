package ci.kossovo.loyer_core_api.events.locations;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ObservedLatePaymentEvent(
    String contratId,
    String locataireId, // Ajout crucial pour notre projection
    YearMonth moisAnnee,
    BigDecimal montantManquant) {}
