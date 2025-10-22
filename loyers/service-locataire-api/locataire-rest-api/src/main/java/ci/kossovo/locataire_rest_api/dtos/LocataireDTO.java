package ci.kossovo.locataire_rest_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Un seul DTO pour la requête et la réponse est souvent suffisant pour une entité simple
public record LocataireDTO(String id, @NotBlank String nom, @NotBlank String prenom, @Email @NotBlank String email,
        String telephone) {

}
