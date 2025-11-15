package ci.kossovo.raiting_service.dtos;

public record CreerNotationRequest(
    String locataireId,
    String contratId,
    int scoreProprete,
    int scoreCommunication,
    int scoreRespectVoisinage,
    int scoreRespectReglement,
    String commentaire,
    String notePar) {}
