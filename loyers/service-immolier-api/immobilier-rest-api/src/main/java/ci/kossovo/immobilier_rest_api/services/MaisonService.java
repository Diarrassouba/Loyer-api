package ci.kossovo.immobilier_rest_api.services;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import java.util.List;

public interface MaisonService {
  // --- Opérations sur les Maisons ---

  // --- Opérations sur les Maisons ---
  MaisonResponseDTO createMaison(MaisonRequestDTO maisonDTO);

  List<MaisonResponseDTO> findAllMaisons();

  MaisonResponseDTO findMaisonById(String id);

  MaisonResponseDTO updateMaison(String id, MaisonRequestDTO maisonDTO);

  void deleteMaison(String id);

  // --- Opérations sur les Appartements ---
  AppartementResponseDTO addAppartementToMaison(
      String maisonId, AppartementRequestDTO appartementDTO);

  List<AppartementResponseDTO> findAppartementsByMaisonId(String maisonId);
  void removeAppartementFromMaison(String maisonId, String appartementId);
}
