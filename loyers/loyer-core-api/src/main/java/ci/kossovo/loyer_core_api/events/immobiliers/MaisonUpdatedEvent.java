package ci.kossovo.loyer_core_api.events.immobiliers;


public record MaisonUpdatedEvent(
    String maisonId,
    String lot,
    String ville,
    String quartier,
    String typeBatiment,
    int anneeConstruction) {}
