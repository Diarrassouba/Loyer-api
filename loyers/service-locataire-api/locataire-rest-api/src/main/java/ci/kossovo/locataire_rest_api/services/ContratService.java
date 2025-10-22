package ci.kossovo.locataire_rest_api.services;

import java.util.List;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireDTO;

public interface ContratService {

    // --- Locataire ---
    LocataireDTO createLocataire(LocataireDTO locataireDTO);

    LocataireDTO findLocataireById(String id);

    List<LocataireDTO> findAllLocataires();

    LocataireDTO updateLocataire(String id, LocataireDTO locataireDTO);

    // --- Contrat ---
    ContratResponseDTO createContrat(ContratRequestDTO contratDTO);

    ContratResponseDTO findContratById(String id);

    ContratResponseDTO terminerContrat(String id);

}
