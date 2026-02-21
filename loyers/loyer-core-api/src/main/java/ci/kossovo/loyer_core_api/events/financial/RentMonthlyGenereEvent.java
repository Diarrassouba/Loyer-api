package ci.kossovo.loyer_core_api.events.financial;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record RentMonthlyGenereEvent(
    String contratId, UUID loyerId, YearMonth moisAnnee, BigDecimal montantDu) {}
