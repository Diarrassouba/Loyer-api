package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;

public record AppartementResponseDTO(
    String id,
    String reference,
    TypeAppartement type,
    int etage,
    int nombreDePieces,
    String maisonId) {}
