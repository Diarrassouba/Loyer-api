package ci.kossovo.immobilier_rest_api.dtos;

public record AppartementResponseDTO(
    String id,
    String reference,
    String description,
    int etage,
    int nombreDePieces,
    String maisonId) {}
