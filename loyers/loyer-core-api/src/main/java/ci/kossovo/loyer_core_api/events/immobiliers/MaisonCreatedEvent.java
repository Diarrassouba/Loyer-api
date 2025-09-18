package ci.kossovo.loyer_core_api.events.immobiliers;

public record MaisonCreatedEvent(
        String maisonId,
        String adresse,
        String ville,
        String quartier,
        String ILot) {

}
