package ci.kossovo.raiting_service.mapper;

import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.dtos.NotationResponseDTO;
import ci.kossovo.raiting_service.models.Notation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(
      target = "dateNotation",
      ignore = true) // Ignorer la date de notation lors de la création
  Notation toNotation(CreerNotationRequest dto);

  // On calcule le score moyen pendant le mapping
  @Mapping(
      target = "scoreMoyen",
      expression =
          "java( (double)(notation.getScoreProprete() + notation.getScoreCommunication() +"
              + " notation.getScoreRespectVoisinage() + notation.getScoreRespectReglement()) / 4.0"
              + " )")
  NotationResponseDTO toNotationResponseDTO(Notation notation);

  List<NotationResponseDTO> toNotationResponseDTOList(List<Notation> notations);
}
