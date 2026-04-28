package ci.kossovo.loyer_core_api.events.immobiliers;

public record AppartementUpdatedEvent(
    String appartementId,
    String reference,
    String maisonId,
    String typeLot,
    String typeBatiment,
    int nombreDePieces,
    String adresseBatiment) {}
