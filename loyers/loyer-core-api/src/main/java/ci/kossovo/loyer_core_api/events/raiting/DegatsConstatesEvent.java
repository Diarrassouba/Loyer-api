package ci.kossovo.loyer_core_api.events.raiting;

import java.math.BigDecimal;

// Événement publié lorsque des dégâts sont constatés pour un contrat. (DamagesNotedEvent)
public record DegatsConstatesEvent(
    String contratId, String locataireId, BigDecimal montant, String description) {}
