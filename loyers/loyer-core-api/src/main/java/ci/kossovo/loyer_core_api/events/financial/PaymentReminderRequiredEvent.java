package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

// Publié quand un simple rappel est nécessaire
public record PaymentReminderRequiredEvent(
    String contratId, String locataireId, BigDecimal soldeActuel // Le montant de la dette
    ) {}
