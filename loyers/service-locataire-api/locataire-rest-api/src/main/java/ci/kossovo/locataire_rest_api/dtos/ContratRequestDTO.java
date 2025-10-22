package ci.kossovo.locataire_rest_api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContratRequestDTO(
    @NotNull @FutureOrPresent LocalDate dateDebut,
    @NotNull @Positive BigDecimal montantLoyerBase,
    @NotBlank String locataireId,
    String appartementId,
    String maisonId
) {

}
