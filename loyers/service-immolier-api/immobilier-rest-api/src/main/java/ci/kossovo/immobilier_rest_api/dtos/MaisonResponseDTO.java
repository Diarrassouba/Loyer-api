package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

public record MaisonResponseDTO(
  String id,
  String iLot,
  String adresse,
  String ville,
  String quartier,
  int anneeConstruction,
  // On inclut la liste des appartements pour avoir une vue complète
  List<AppartementDTO> appartements,
 // List<DepenseDTO> depenses
) {}