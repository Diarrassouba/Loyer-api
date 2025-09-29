package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

public record MaisonResponseDTO(
    String maisonId,
    String ilot,
    String quartier,
    String ville,
    int anneeConstruction,
    // On inclut la liste des appartements pour avoir une vue complète
    List<AppartementResponseDTO> appartements
    // List<DepenseDTO> depenses
    ) {}
