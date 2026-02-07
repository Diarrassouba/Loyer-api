package ci.kossovo.raiting_service.services;

import java.util.List;

import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.dtos.NotationResponseDTO;

public interface NotationService {
    
    /**
     * Crée une nouvelle notation, la persiste et publie un événement.
     * @param requestDTO Les données de la notation à créer.
     * @return Le DTO de la notation créée.
     */
    NotationResponseDTO createNotation(CreerNotationRequest requestDTO);
    
    /**
     * Récupère toutes les notations pour un locataire spécifique.
     * @param locataireId L'ID du locataire.
     * @return Une liste de DTOs des notations.
     */
    List<NotationResponseDTO> findNotationsByLocataireId(String locataireId);
    
    /**
     * Récupère une notation par son ID.
     * @param id L'ID de la notation.
     * @return Le DTO de la notation trouvée.
     */
    NotationResponseDTO findNotationById(String id);
    List<NotationResponseDTO> findAllNotations();
}
