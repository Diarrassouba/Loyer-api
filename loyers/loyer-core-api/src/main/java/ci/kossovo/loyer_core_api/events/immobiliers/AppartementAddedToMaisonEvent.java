package ci.kossovo.loyer_core_api.events.immobiliers;

public record AppartementAddedToMaisonEvent(
    String appartementId, String maisonId, String reference) {}
