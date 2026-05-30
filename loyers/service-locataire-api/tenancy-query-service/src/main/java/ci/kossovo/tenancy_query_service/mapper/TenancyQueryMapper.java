package ci.kossovo.tenancy_query_service.mapper;

import ci.kossovo.tenancy_query_service.dtos.AppartementItemDTO;
import ci.kossovo.tenancy_query_service.dtos.MaisonCompleteDTO;
import ci.kossovo.tenancy_query_service.model.AppartementItem;
import ci.kossovo.tenancy_query_service.model.MaisonDocument;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TenancyQueryMapper {
  AppartementItemDTO toAppartementItemDTO(AppartementItem item);

  MaisonCompleteDTO toMaisonCompleteDTO(MaisonDocument doc);

  List<MaisonCompleteDTO> toMaisonCompleteDTOList(List<MaisonDocument> docs);
}
