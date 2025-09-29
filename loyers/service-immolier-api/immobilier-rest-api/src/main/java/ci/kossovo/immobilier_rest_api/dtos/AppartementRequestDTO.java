package ci.kossovo.immobilier_rest_api.dtos;

<<<<<<< HEAD
=======
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
import jakarta.validation.constraints.NotBlank;

public record AppartementRequestDTO(
    @NotBlank(message = "La référence ne peut pas être vide") String reference,
<<<<<<< HEAD
    String description,
    double surface,
=======
    TypeAppartement type,
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
    int etage,
    int nombreDePieces) {}
