package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;

public record AppartementResponseDTO(
    String id,
    String reference,
<<<<<<< HEAD
    String description,
=======
    TypeAppartement type,
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
    int etage,
    int nombreDePieces,
    String maisonId) {}
