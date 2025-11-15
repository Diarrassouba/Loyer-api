package ci.kossovo.loyer_core_api.events.raiting;

import java.time.LocalDate;

public record TenantNoteEvent(
    String notationId,
    String locataireId,
    String contratId,
    double scoreMoyen, // Un score calculé, plus simple à consommer pour les autres services
    LocalDate dateNotation) {}
