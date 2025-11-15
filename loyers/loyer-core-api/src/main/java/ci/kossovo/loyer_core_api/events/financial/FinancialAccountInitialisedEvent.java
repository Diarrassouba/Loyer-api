package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

public record FinancialAccountInitialisedEvent(
    String contratId, String locataireId, BigDecimal montantLoyerMensuel) {}
