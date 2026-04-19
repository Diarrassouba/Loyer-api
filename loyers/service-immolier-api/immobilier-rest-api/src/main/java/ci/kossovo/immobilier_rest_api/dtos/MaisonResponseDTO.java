package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeMaison;

public record MaisonResponseDTO(
    String id,
    String lot,
    String quartier,
    String ville,
    TypeMaison type,
    int anneeConstruction,
    // On inclut la liste des appartements pour avoir une vue complète
    List<AppartementResponseDTO> appartements
    // List<DepenseDTO> depenses
    ) {}
