package ci.kossovo.immobilier_rest_api.dtos;

public record AppartementResponseDTO(
    String id,
    String reference,
    String description,
    double surface,
    int etage,
    int nombreDePieces,
    int nombreSallesBains,
    String maisonId) {}
