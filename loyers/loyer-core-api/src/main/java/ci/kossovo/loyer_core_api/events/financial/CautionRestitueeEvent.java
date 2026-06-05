package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

// Événement publié lorsque la caution d'un contrat est restituée. (DepositReturnedEvent)
public record CautionRestitueeEvent(
    String contratId, BigDecimal montantRestitue, BigDecimal nouveauSolde) {}
