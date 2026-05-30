package ci.kossovo.tenancy_query_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import ci.kossovo.tenancy_query_service.dtos.MaisonCompleteDTO;
import ci.kossovo.tenancy_query_service.exception.ResourceNotFoundException;
import ci.kossovo.tenancy_query_service.mapper.TenancyQueryMapper;
import ci.kossovo.tenancy_query_service.repository.MaisonDocumentRepository;
import ci.kossovo.tenancy_query_service.service.ConsultationService;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    private final MaisonDocumentRepository maisonRepo;
    private final TenancyQueryMapper mapper;

    public ConsultationServiceImpl(MaisonDocumentRepository maisonRepo, TenancyQueryMapper mapper) {
        this.maisonRepo = maisonRepo;
        this.mapper = mapper;
    }

    @Override
    public MaisonCompleteDTO getMaisonCompleteById(String maisonId) {
        return maisonRepo.findById(maisonId)
                .map(mapper::toMaisonCompleteDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Maison introuvable: " + maisonId));
    }

    @Override
    public List<MaisonCompleteDTO> getAllMaisonsCompletes() {
        return mapper.toMaisonCompleteDTOList(maisonRepo.findAll());
    }
}
