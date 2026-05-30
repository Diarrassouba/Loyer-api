package ci.kossovo.tenancy_query_service.dtos;

import java.util.List;

public record MaisonCompleteDTO(
    String maisonId,
    boolean loueeEnEntier,
    int totalAppartements,
    int appartementsLoues,
    List<AppartementItemDTO> appartements // La liste des enfants
    ) {}
