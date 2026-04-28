package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeLot;

public record AppartementResponseDTO(
    String id, String reference, TypeLot typeLot, int etage, int nombreDePieces, String maisonId) {}
