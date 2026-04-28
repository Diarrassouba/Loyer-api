package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeLot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppartementRequestDTO(
    @NotBlank(message = "La référence ne peut pas être vide") String reference,
    @NotNull(message = "Le type de lot doit être spécifié") TypeLot typeLot,
    int etage,
    int nombreDePieces) {}
