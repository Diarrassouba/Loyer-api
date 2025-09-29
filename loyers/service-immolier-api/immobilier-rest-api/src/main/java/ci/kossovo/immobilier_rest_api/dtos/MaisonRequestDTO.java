package ci.kossovo.immobilier_rest_api.dtos;

import jakarta.validation.constraints.NotBlank;

public record MaisonRequestDTO(
<<<<<<< HEAD
    @NotBlank(message = "La ville ne peut pas être vide") String ilot,
=======
    @NotBlank(message = "La ville ne peut pas être vide") String lot,
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
    @NotBlank(message = "Le pays ne peut pas être vide") String quartier,
    @NotBlank(message = "La région ne peut pas être vide") String ville,
    int anneeConstruction) {}
