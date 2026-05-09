package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record RentMonthlyGeneredEvent(
    String contratId,
    UUID loyerId,
    String locataireId,
    YearMonth moisAnnee,
    BigDecimal montantDu,
    BigDecimal nouveauSolde) {}
