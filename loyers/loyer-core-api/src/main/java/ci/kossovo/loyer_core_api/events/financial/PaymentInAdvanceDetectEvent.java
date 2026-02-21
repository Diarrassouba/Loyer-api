package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;

public record PaymentInAdvanceDetectEvent(
    String contratId, String locataireId, BigDecimal montantEnAvance) {}
