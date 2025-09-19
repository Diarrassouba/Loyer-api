package ci.kossovo.immobilier_rest_api.dtos;

public record MaisonRequestDTO(
  @NotBlank(message = "L'adresse ne peut pas être vide") String adresse,

  @NotBlank(message = "La ville ne peut pas être vide") String ville,

  @NotBlank(message = "Le code postal ne peut pas être vide")
  @Size(
    min = 5,
    max = 10,
    message = "Le code postal doit contenir entre 5 et 10 caractères"
  )
  String codePostal,
  @NotBlank(message = "Le quartier ne peut pas être vide") String quartier,
  int anneeConstruction
) {}
