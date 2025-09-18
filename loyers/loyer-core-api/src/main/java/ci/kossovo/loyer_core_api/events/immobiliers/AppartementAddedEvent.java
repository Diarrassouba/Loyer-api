package ci.kossovo.loyer_core_api.events.immobiliers;

public record AppartementAddedEvent(
        String maisonId,
        String appartementId,
        String reference,
        String type,
        int nombreDePieces) {

}
