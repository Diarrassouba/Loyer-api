package ci.kossovo.tenancy_query_service.service;

import java.util.List;

import ci.kossovo.tenancy_query_service.dtos.MaisonCompleteDTO;

public interface ConsultationService {

    MaisonCompleteDTO getMaisonCompleteById(String maisonId);
    List<MaisonCompleteDTO> getAllMaisonsCompletes();
}
