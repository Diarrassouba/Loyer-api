package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRequestDTO(
    String id,
    BigDecimal montant,
    String description,
    LocalDate date,
    TypeDepense typeDepense,
    String maisonId,
    String appartementId) {}
