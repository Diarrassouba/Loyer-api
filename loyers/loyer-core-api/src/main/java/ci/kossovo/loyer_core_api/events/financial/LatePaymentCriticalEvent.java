package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

// Publié quand la situation devient critique (ex: plus de 30 jours de retard)
public record LatePaymentCriticalEvent(
    String contratId, String locataireId, BigDecimal soldeActuel, int joursDeRetardEstimes) {}
