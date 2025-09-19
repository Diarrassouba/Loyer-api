package ci.kossovo.immobilier_rest_api.services.impl;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.events.AppartementAddedToMaisonEvent;
import ci.kossovo.immobilier_rest_api.events.MaisonCreatedEvent;

import ci.kossovo.immobilier_rest_api.mappers.PropertyMapper;
import ci.kossovo.immobilier_rest_api.models.Appartement;
import ci.kossovo.immobilier_rest_api.models.Maison;
import ci.kossovo.immobilier_rest_api.repositories.AppartementRepository;
import ci.kossovo.immobilier_rest_api.repositories.MaisonRepository;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
// Custom exception for Maison not found
import ci.kossovo.loyer_core_api.exceptions.MaisonNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.cloud.stream.function.EventGateway;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional // Assure que chaque méthode est exécutée dans une transaction
public class MaisonServiceImpl implements MaisonService {

  private final MaisonRepository maisonRepository;
  private final AppartementRepository appartementRepository;
  private final PropertyMapper mapper;
  private final EventGateway eventGateway;

  public MaisonServiceImpl(
    MaisonRepository maisonRepository,
    AppartementRepository appartementRepository,
    PropertyMapper mapper,
    EventGateway eventGateway
  ) {
    this.maisonRepository = maisonRepository;
    this.appartementRepository = appartementRepository;
    this.mapper = mapper;
    this.eventGateway = eventGateway;
  }

  // --- Opérations sur les Maisons ---
  @Override
  public MaisonResponseDTO createMaison(MaisonRequestDTO maisonDTO) {
    // 1. Mapper le DTO en entité
    Maison maison = mapper.toMaison(maisonDTO);
    // 2. Persister l'entité
    maison = maisonRepository.save(maison);
    // 3. Publier l'événement de domaine
    eventGateway.publishEvent(
      new MaisonCreatedEvent(
        maison.getId(),
        maison.getILot(),
        maison.getAdresse(),
        maison.getVille(),
        maison.getQuartier(),
        maison.getAnneeConstruction()
      )
    );

    // 4. Mapper l'entité sauvegardée en DTO de réponse
    return mapper.toMaisonResponseDTO(maison);
  }

  @Override
  @Transactional(readOnly = true) // Optimisation pour les lectures
  public List<MaisonResponseDTO> findAllMaisons() {
    List<Maison> maisons = maisonRepository.findAll();
    return mapper.toMaisonResponseDTOs(maisons);
  }

  @Override
  @Transactional(readOnly = true) // Optimisation pour les lectures
  public MaisonResponseDTO findMaisonById(String id) {
    Maison maison = maisonRepository
      .findById(id)
      .orElseThrow(() ->
        new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id)
      );

    return mapper.toMaisonResponseDTO(maison);
  }

  @Override
  public MaisonResponseDTO updateMaison(String id, MaisonRequestDTO maisonDTO) {
    Maison maison = maisonRepository
      .findById(id)
      .orElseThrow(() ->
        new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id)
      );

    mapper.updateMaisonFromDto(maisonDTO, maison);
    Maison updatedMaison = maisonRepository.save(maison);
    // On pourrait publier un événement MaisonMiseAJourEvenement si nécessaire
    eventGateway.publishEvent(
      new MaisonCreatedEvent(
        updatedMaison.getId(),
        updatedMaison.getILot(),
        updatedMaison.getAdresse(),
        updatedMaison.getVille(),
        updatedMaison.getQuartier(),
        updatedMaison.getAnneeConstruction()
      )
    );
    return mapper.toMaisonResponseDTO(updatedMaison);
  }

  @Override
  public void deleteMaison(String id) {
    if (!maisonRepository.existsById(id)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id);
    }
    maisonRepository.deleteById(id);
    // Publier un événement MaisonSupprimeeEvenement si nécessaire
  }

  // --- Opérations sur les Appartements ---
  @Override
  public AppartementResponseDTO addAppartementToMaison(
    String maisonId,
    AppartementRequestDTO appartementDTO
  ) {
    Maison maison = maisonRepository
      .findById(maisonId)
      .orElseThrow(() ->
        new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId)
      );

    // Mapper le DTO en entité
    Appartement appartement = mapper.toAppartement(appartementDTO);
    maison.addAppartement(appartement);
    // Persister l'appartement (grâce au cascade, la maison est aussi persistée)
    //appartementRepository.save(appartement);

    // La sauvegarde de la maison persiste aussi l'appartement
    maisonRepository.save(maison);

    eventGateway.publishEvent(
      new AppartementAddedToMaisonEvent(
        appartement.getId(),
        appartement.getReference(),
        appartement.getDescription(),
        appartement.getSurface(),
        appartement.getMaison().getId()
      )
    );

    // Mapper l'entité sauvegardée en DTO de réponse
    return mapper.toAppResponseDTO(appartement);
  }

  @Override
  public List<AppartementResponseDTO> findAppartementsByMaisonId(
    String maisonId
  ) {
    if (!maisonRepository.existsById(maisonId)) {
      throw new MaisonNotFoundException(
        "Maison non trouvée avec l'ID: " + maisonId
      );
    }

    return mapper.toAppartementResponseDTOList(
      appartementRepository.findByMaisonId(maisonId)
    );
  }
}
