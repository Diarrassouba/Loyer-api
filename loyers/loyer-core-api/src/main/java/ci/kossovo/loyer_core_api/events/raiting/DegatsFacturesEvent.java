package ci.kossovo.loyer_core_api.events.raiting;

import java.math.BigDecimal;

// Événement publié lorsque des dégâts sont facturés pour un contrat. (DamagesInvoicedEvent)
public record DegatsFacturesEvent(
    String contratId, BigDecimal montantDegats, String description, BigDecimal nouveauSolde) {}
