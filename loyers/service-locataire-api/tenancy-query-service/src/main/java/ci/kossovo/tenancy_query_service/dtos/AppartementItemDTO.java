package ci.kossovo.tenancy_query_service.dtos;

public record AppartementItemDTO(
    String appartementId,
    String reference,
    boolean loue
    // On n'inclut pas maisonId ici car il est redondant quand 
    // cet objet est inclus dans la liste d'une MaisonCompleteDTO.
) {}
