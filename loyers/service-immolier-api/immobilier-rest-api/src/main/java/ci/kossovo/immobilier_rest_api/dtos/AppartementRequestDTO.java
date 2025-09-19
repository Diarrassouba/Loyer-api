package ci.kossovo.immobilier_rest_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppartementRequestDTO(
  @NotBlank(message = "La référence ne peut pas être vide") String reference,
  @NotBlank(message = "La description ne peut pas être vide")
  String description,
  @NotNull(message = "La surface ne peut pas être nulle")
  @Min(value = 1, message = "La surface doit être d'au moins 1")
  double surface,
  int etage,
  @NotNull(message = "Le nombre de pièces ne peut pas être nul")
  @Min(value = 1, message = "Le nombre de pièces doit être d'au moins 1")
  int nombreDePieces,
  @NotNull(message = "Le nombre de salles de bains ne peut pas être nul")
  @Min(
    value = 0,
    message = "Le nombre de salles de bains ne peut pas être négatif"
  )
  int nombreSallesBains
) {}
