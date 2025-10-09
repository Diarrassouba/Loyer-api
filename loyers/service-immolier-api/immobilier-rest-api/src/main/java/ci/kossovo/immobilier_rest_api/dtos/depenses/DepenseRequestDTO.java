package ci.kossovo.immobilier_rest_api.dtos.depenses;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRequestDTO(@NotNull @Positive BigDecimal montant, @NotBlank String description,

                @NotNull LocalDate date,

                @NotNull TypeDepense typeDepense,

                // Le client doit fournir l'un des deux, mais pas les deux.
                // La validation de cette logique se fera dans le service.
                String maisonId, String appartementId) {
}
