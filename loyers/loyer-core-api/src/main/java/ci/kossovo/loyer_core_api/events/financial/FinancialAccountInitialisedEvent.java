package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

public record FinancialAccountInitialisedEvent(
    String contratId,
    String locataireId,
    String bienId,
    BigDecimal montantLoyerMensuel,
    BigDecimal montantCaution,
    BigDecimal montantAvance,
    BigDecimal soldeInitial // NOUVEAU : Sera calculé comme -(montantCaution)
    ) {}
