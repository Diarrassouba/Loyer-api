package ci.kossovo.locataire_rest_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Un seul DTO pour la requête et la réponse est souvent suffisant pour une entité simple
public record LocataireRequestDTO(@NotBlank(message = "Le nom est obligatoire") String nom,
                @NotBlank(message = "Le prénom est obligatoire") String prenom,
                @Email @NotBlank(message = "L'email est obligatoire") String email, String telephone) {

}
