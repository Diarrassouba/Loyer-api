package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

public record MaisonDTO(
    String maisonId,
    String numLot,
    String ville,
    String quartier,
    int anneeConstruction,
    String adresse,
    List<AppartementDTO> appartements) {}
