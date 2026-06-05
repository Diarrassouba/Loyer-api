package ci.kossovo.raiting_service.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreerNotationRequest(
    @NotBlank String locataireId,
    @NotBlank String contratId,
    @Min(value = 1, message = "Le score doit être au minimum de 1")
        @Max(value = 5, message = "Le score doit être au maximum de 5")
        int scoreProprete,
    @Min(1) @Max(5) // Version courte
        int scoreCommunication,
    @Min(1) @Max(5) int scoreRespectVoisinage,
    @Min(1) @Max(5) int scoreRespectReglement,
    String commentaire,
    @NotBlank String notePar,
    BigDecimal coutReparations,
    String descriptionDegats) {}
