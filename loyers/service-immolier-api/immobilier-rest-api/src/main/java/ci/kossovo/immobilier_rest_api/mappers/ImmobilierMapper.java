package ci.kossovo.immobilier_rest_api.mappers;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.model.Appartement;
import ci.kossovo.immobilier_rest_api.model.Maison;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImmobilierMapper {
  // --- MAPPERS POUR MAISON ---

<<<<<<< HEAD
  // @Mapping(source = "id", target = "maisonId")
  @Mapping(source = "id", target = "maisonId")
=======
  @Mapping(source = "appartements", target = "appartements")
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
  MaisonResponseDTO toMaisonResponseDTO(Maison maison);

  // @Mapping(source = "id", target = "maisonId")
  List<MaisonResponseDTO> toMaisonResponseDTOs(List<Maison> maisons);

<<<<<<< HEAD
  // @Mapping(target = "id", ignore = true)
=======
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appartements", ignore = true)
  Maison toMaison(MaisonRequestDTO dto);

  // Pour les mises à jour (PUT), pour ne pas écraser l'ID
<<<<<<< HEAD
=======

>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appartements", ignore = true)
  void updateMaisonFromDto(MaisonRequestDTO dto, @MappingTarget Maison maison);

  // --- MAPPERS POUR APPARTEMENT ---

  @Mapping(source = "maison.id", target = "maisonId") // Mapper manuellement maison.id -> maisonId
  AppartementResponseDTO toAppResponseDTO(Appartement appartement);

  @Mapping(source = "maison.id", target = "maisonId")
  List<AppartementResponseDTO> toAppartementsResponseDTOs(List<Appartement> appartements);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "maison", ignore = true)
  Appartement toAppartement(AppartementRequestDTO dto);

  // Pour les mises à jour (PUT), pour ne pas écraser l'ID
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "maison", ignore = true)
  void updateAppartementFromDto(AppartementRequestDTO dto, @MappingTarget Appartement appartement);
}
