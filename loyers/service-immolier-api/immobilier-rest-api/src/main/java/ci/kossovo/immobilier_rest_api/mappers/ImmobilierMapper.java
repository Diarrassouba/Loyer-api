package ci.kossovo.immobilier_rest_api.mappers;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.entities.Appartement;
import ci.kossovo.immobilier_rest_api.entities.Maison;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImmobilierMapper {
  // --- MAPPERS POUR MAISON ---
  MaisonResponseDTO toMaisonResponseDTO(Maison maison);
  List<MaisonResponseDTO> toMaisonResponseDTOs(List<Maison> maisons);

  Maison toMaison(MaisonRequestDTO dto);
  // Pour les mises à jour (PUT), pour ne pas écraser l'ID
  void updateMaisonFromDto(MaisonRequestDTO dto, @MappingTarget Maison maison);

  // --- MAPPERS POUR APPARTEMENT ---

  @Mapping(source = "maison.id", target = "maisonId") // Mapper manuellement maison.id -> maisonId
  AppartementResponseDTO toAppResponseDTO(Appartement appartement);

  List<AppartementResponseDTO> toAppResponseDTOs(
    List<Appartement> appartements
  );
  Appartement toAppartement(AppartementRequestDTO dto);
  // Pour les mises à jour (PUT), pour ne pas écraser l'ID
  void updateAppartementFromDto(
    AppartementRequestDTO dto,
    @MappingTarget Appartement appartement
  );
}
