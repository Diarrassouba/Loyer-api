package ci.kossovo.locataire_rest_api.services;

import java.util.List;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireResponseDto;

public interface ContratService {

    // --- Locataire ---
    LocataireResponseDto createLocataire(LocataireRequestDTO locataireDTO);

    LocataireResponseDto findLocataireById(String id);

    List<LocataireResponseDto> findAllLocataires();

    LocataireResponseDto updateLocataire(String id, LocataireRequestDTO locataireDTO);
    // --- Contrat ---
    ContratResponseDTO createContrat(ContratRequestDTO contratDTO);

    ContratResponseDTO findContratById(String id);

    ContratResponseDTO terminerContrat(String id);

}
