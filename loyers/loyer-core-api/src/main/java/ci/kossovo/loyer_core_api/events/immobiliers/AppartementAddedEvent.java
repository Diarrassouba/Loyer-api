package ci.kossovo.loyer_core_api.events.immobiliers;

public record AppartementAddedEvent(
    String appartementId, String reference, String maisonId, String type, int nombreDePieces) {}
