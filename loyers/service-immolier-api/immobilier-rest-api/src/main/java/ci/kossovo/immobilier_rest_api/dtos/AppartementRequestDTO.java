package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;
import jakarta.validation.constraints.NotBlank;

public record AppartementRequestDTO(
    @NotBlank(message = "La référence ne peut pas être vide") String reference,
    TypeAppartement type,
    int etage,
    int nombreDePieces) {}
