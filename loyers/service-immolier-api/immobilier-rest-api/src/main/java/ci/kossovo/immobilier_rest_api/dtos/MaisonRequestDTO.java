package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeMaison;
import jakarta.validation.constraints.NotBlank;

public record MaisonRequestDTO(
    @NotBlank(message = "La ville ne peut pas être vide") String lot,
    @NotBlank(message = "Le pays ne peut pas être vide") String quartier,
    @NotBlank(message = "La région ne peut pas être vide") String ville,
    TypeMaison type,
    int anneeConstruction) {}
