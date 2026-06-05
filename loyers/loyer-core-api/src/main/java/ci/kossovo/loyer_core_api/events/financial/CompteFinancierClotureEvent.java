package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

// Événement publié lorsque le compte financier d'un contrat est clôturé.
// (FinancialAccountClosedEvent)
public record CompteFinancierClotureEvent(
    String contratId, BigDecimal montantRembourse, BigDecimal nouveauSolde) {}
