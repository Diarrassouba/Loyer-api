package ci.kossovo.immobilier_rest_api.dtos;

import jakarta.validation.constraints.NotBlank;

public record AppartementRequestDTO(
    @NotBlank(message = "La référence ne peut pas être vide") String reference,
    String description,
    double surface,
    int etage,
    int nombreDePieces) {}
