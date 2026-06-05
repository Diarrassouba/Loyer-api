package ci.kossovo.locataire_rest_api.mappers;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireResponseDto;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.Locataire;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TenancyMapper {

  // ===================================================================
  // MAPPERS POUR LOCATAIRE
  // ===================================================================

  LocataireDTO toLocataireDTO(Locataire locataire);

  List<LocataireDTO> toLocataireDTOList(List<Locataire> locataires);

  /**
   * MapStruct ignore automatiquement le champ "id" lors de la création car il est généré dans
   * l'entité.
   */
  @Mapping(target = "id", ignore = true)
  Locataire toLocataire(LocataireRequestDTO dto);

  LocataireResponseDto toLocataireResponseDto(Locataire locataire);

  List<LocataireResponseDto> toLocataireResponseDtoList(List<Locataire> locataires);

  @Mapping(target = "nom", source = "locataireDTO.nom")
  @Mapping(target = "prenom", source = "locataireDTO.prenom")
  @Mapping(target = "email", source = "locataireDTO.email")
  @Mapping(target = "telephone", source = "locataireDTO.telephone")
  Locataire toLocataire(LocataireRequestDTO locataireDTO, Locataire existingLocataire);

  // ===================================================================
  // MAPPERS POUR CONTRAT
  // ===================================================================

  /**
   * Convertit la requête entrante en Entité JPA. Les champs communs (locataireId, dateDebut, etc.)
   * sont mappés automatiquement. Le "typeBien" (MAISON ou APPARTEMENT) est défini manuellement dans
   * le Service avant la sauvegarde, car il nécessite une règle de gestion.
   */
  @Mapping(target = "actif", ignore = true)
  @Mapping(target = "dateFin", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(
      target = "typeBien",
      ignore = true) // Le service définira ce champ en fonction de la présence de maisonId ou
  // appartementId
  @Mapping(target = "montantCaution", ignore = true) // Calculé dans le service (2 mois de loyer)
  ContratLocation toContratLocation(ContratRequestDTO dto);

  /**
   * Construit la réponse unifiée pour l'API.
   *
   * <p>Astuce MapStruct : L'entité ContratLocation possède soit un maisonId, soit un appartementId.
   * Le DTO de réponse attend un seul champ "bienId" unifié. On utilise "expression = java(...)"
   * pour intégrer directement la logique Java dans le code généré par MapStruct (l'équivalent de
   * notre Optional.ofNullable...orElse).
   */
  @Mapping(
      target = "bienId",
      expression =
          "java( contrat.getMaisonId() != null ? contrat.getMaisonId() : contrat.getAppartementId()"
              + " )")
  ContratResponseDTO toContratResponseDTO(ContratLocation contrat);

  List<ContratResponseDTO> toContratResponseDTOList(List<ContratLocation> contrats);

  /* // --- Contrat ---

   @Mapping(target = "actif", ignore = true)
  @Mapping(target = "dateFin", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appartementId", ignore = true)
  @Mapping(target = "maisonId", source = "maisonId")
  ContratLocation toContratLocation(ContratMaisonRequestDTO dto);

  @Mapping(target = "actif", ignore = true)
  @Mapping(target = "dateFin", ignore = true)
  @Mapping(target = "id", ignore = true)

  ContratLocation toContratLocation(ContratAppartementRequestDTO dto);

  @Mapping(
      target = "bienId",
      expression =
          "java(contrat.getMaisonId() != null ? contrat.getMaisonId() :"
              + " contrat.getAppartementId())")
  @Mapping(
      target = "typeBien",
      expression = "java(contrat.getMaisonId() != null ? \"MAISON\" : \"APPARTEMENT\")")
  ContratResponseDTO toContratResponseDTO(ContratLocation contrat);

  List<ContratResponseDTO> toContratResponseDTOs(List<ContratLocation> contrats);

  List<LocataireResponseDto> toLocataireDTOs(List<Locataire> locataires); */
}
