package ci.kossovo.loyer_core_api.events.immobiliers;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRecordedEvent(
    String depenseId,
    String bienImmobilierId,
    String typeBien,
    BigDecimal montant,
    String description,
    LocalDate date) {

}
