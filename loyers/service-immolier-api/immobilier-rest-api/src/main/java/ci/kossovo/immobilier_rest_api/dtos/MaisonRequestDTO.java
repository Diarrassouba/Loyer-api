package ci.kossovo.immobilier_rest_api.dtos;

import jakarta.validation.constraints.NotBlank;

public record MaisonRequestDTO(
    @NotBlank(message = "L'adresse ne peut pas être vide") String adresse,
    @NotBlank(message = "La ville ne peut pas être vide") String ilot,
    @NotBlank(message = "Le pays ne peut pas être vide") String quartier,
    @NotBlank(message = "La région ne peut pas être vide") String ville,
    @NotBlank(message = "Le code postal ne peut pas être vide") String codePostal,
    int anneeConstruction) {}
