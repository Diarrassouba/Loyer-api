package ci.kossovo.locataire_rest_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LocataireDTO(

    // L'id est null lors de la création (requête POST), mais rempli lors de la réponse.
    String id,
    @NotBlank(message = "Le nom du locataire est obligatoire") String nom,
    @NotBlank(message = "Le prénom du locataire est obligatoire") String prenom,
    @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Le format de l'adresse email est invalide")
        String email,

    // Validation basique pour un numéro de téléphone (optionnelle mais recommandée)
    // Ici, une regex simple qui accepte les numéros internationaux ou locaux
    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Le format du numéro de téléphone est invalide")
        String telephone) {}
