package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeBatiment;
import java.util.List;

public record MaisonResponseDTO(
    String id,
    String lot,
    String quartier,
    String ville,
    TypeBatiment typeBatiment,
    int anneeConstruction,
    // On inclut la liste des appartements pour avoir une vue complète
    List<AppartementResponseDTO> appartements
    // List<DepenseDTO> depenses
    ) {}
