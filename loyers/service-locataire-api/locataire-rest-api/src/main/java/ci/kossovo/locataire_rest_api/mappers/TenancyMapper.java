package ci.kossovo.locataire_rest_api.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireDTO;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.Locataire;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TenancyMapper {

    // --- Locataire ---
    LocataireDTO toLocataireDTO(Locataire locataire);

    Locataire toLocataire(LocataireDTO dto);

    // --- Contrat ---

    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "dateFin", ignore = true)
    @Mapping(target = "id", ignore = true)
    ContratLocation toContratLocation(ContratRequestDTO dto);

    @Mapping(target = "bienId", expression = "java(contrat.getMaisonId() != null ? contrat.getMaisonId() : contrat.getAppartementId())")
    @Mapping(target = "typeBien", expression = "java(contrat.getMaisonId() != null ? \"MAISON\" : \"APPARTEMENT\")")
    ContratResponseDTO toContratResponseDTO(ContratLocation contrat);

    List<ContratResponseDTO> toContratResponseDTOs(List<ContratLocation> contrats);

    List<LocataireDTO> toLocataireDTOs(List<Locataire> locataires);

}
