package ci.kossovo.raiting_service.dtos;

import java.time.LocalDate;

public record NotationResponseDTO(
    String id,
    String locataireId,
    String contratId,
    LocalDate dateNotation,
    int scoreProprete,
    int scoreCommunication,
    int scoreRespectVoisinage,
    int scoreRespectReglement,
    String commentaire,
    String notePar,
    double scoreMoyen // Inclure le score moyen calculé
    ) {}
