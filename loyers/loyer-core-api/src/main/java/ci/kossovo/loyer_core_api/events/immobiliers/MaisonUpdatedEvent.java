package ci.kossovo.loyer_core_api.events.immobiliers;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeMaison;

public record MaisonUpdatedEvent(
    String maisonId,
    String lot,
    String ville,
    String quartier,
    TypeMaison type,
    int anneeConstruction) {}
