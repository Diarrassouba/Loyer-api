package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.YearMonth;

public record RetardDePaiementDetecteEvent(
    String contratId, String locataireId, YearMonth moisAnnee, BigDecimal montantEnRetard) {}
