package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

public record MaisonDTO(
  String id,
  String iLot,
  String adresse,
  String ville,
  String quartier,
  int anneeConstruction,
  List<AppartementDTO> appartements
) {}
