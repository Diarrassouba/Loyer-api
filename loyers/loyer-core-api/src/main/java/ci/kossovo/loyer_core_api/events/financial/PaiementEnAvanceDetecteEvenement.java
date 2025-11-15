package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

public record PaiementEnAvanceDetecteEvenement(String contratId, BigDecimal montantEnAvance) {}
