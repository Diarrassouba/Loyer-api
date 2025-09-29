/* package ci.kossovo.immobilier_rest_api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.model.Maison;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.model.Appartement;



@Mapper(componentModel = "spring")
public interface MaisonMapper {
    // --- MAPPERS POUR MAISON ---
    
    // Mapper une entité Maison vers un DTO de réponse
    @Mapping(source = "id", target = "maisonId") // Mapper manuellement id -> maisonId
    MaisonResponseDTO toMaisonResponseDTO(Maison maison);
    
    // Mapper une liste d'entités Maison vers une liste de DTOs de réponse
     @Mapping(source = "id", target = "maisonId")
    List<MaisonResponseDTO> toMaisonResponseDTOs(List<Maison> maisons);
    
    // Mapper un DTO de requête vers une entité Maison
    @Mapping(source = "numeroLot", target = "iLot") 
    Maison toMaison(MaisonRequestDTO dto);

    @Mapping(source = "id", target = "appartementId") // Mapper manuellement id -> appartementId
    AppartementResponseDTO toAppartementDTO(Appartement appartement);

    // Mapper un DTO de réponse vers une entité Appartement
    @Mapping(source = "appartementId", target = "id") // Mapper manuellement appartementId -> id
    Appartement toAppartement(AppartementResponseDTO dto);
}

  */