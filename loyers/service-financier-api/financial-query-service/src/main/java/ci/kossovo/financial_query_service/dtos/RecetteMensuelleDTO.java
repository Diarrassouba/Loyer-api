package ci.kossovo.financial_query_service.dtos;

import java.math.BigDecimal;

public record RecetteMensuelleDTO(
    String mois, // Ex: "2023-10"
    BigDecimal totalEncaisse
) {}
