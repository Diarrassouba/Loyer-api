package ci.kossovo.immobilier_rest_api.dtos;

public record AppartementDTO(
    String appartementId,
    String reference,
    String description,
    double surface,
    int etage,
    int nombreDePieces,
    String maisonId) {}
