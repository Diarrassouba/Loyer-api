package ci.kossovo.loyer_core_api.events.locations;

import java.time.LocalDate;

public record ContratFinishedEvent(
    String contratId,
    String locataireId,
    String bienId, // Important pour mettre à jour
    String typeBien, // "MAISON" ou "APPARTEMENT"
    LocalDate dateFinEffective) {}
