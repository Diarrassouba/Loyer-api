package ci.kossovo.immobilier_rest_api.services.impl;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseResponseDTO;
import ci.kossovo.immobilier_rest_api.mappers.ImmobilierMapper;
import ci.kossovo.immobilier_rest_api.model.Appartement;
import ci.kossovo.immobilier_rest_api.model.Depense;
import ci.kossovo.immobilier_rest_api.model.Maison;
import ci.kossovo.immobilier_rest_api.repositories.AppartementRepository;
import ci.kossovo.immobilier_rest_api.repositories.DepenseRepository;
import ci.kossovo.immobilier_rest_api.repositories.MaisonRepository;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedToMaisonEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementDeletedToMaisonEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementUpdatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.DepenseDeletedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.DepenseRecordedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.DepenseUpdatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonDeletedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonUpdatedEvent;
// Custom exception for Maison not found
import ci.kossovo.loyer_core_api.exceptions.MaisonNotFoundException;
import java.util.List;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Assure que chaque méthode est exécutée dans une transaction
public class MaisonServiceImpl implements MaisonService {

  private final MaisonRepository maisonRepository;
  private final AppartementRepository appartementRepository;
  private final DepenseRepository depenseRepository;
  private final ImmobilierMapper mapper;
  private final EventGateway eventGateway;

  public MaisonServiceImpl(
      MaisonRepository maisonRepository,
      AppartementRepository appartementRepository,
      DepenseRepository depenseRepository,
      ImmobilierMapper mapper,
      EventGateway eventGateway) {
    this.maisonRepository = maisonRepository;
    this.appartementRepository = appartementRepository;
    this.depenseRepository = depenseRepository;
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

    eventGateway.publish(
        new MaisonCreatedEvent(
            maison.getId(),
            maison.getLot(),
            maison.getVille(),
            maison.getQuartier(),
            maison.getType().toString(),
            maison.getAnneeConstruction()));

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
    Maison maison =
        maisonRepository
            .findById(id)
            .orElseThrow(() -> new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id));

    return mapper.toMaisonResponseDTO(maison);
  }

  @Override
  public MaisonResponseDTO updateMaison(String id, MaisonRequestDTO maisonDTO) {
    Maison maison =
        maisonRepository
            .findById(id)
            .orElseThrow(() -> new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id));

    mapper.updateMaisonFromDto(maisonDTO, maison);
    Maison updatedMaison = maisonRepository.save(maison);

    // On pourrait publier un événement MaisonMiseAJourEvenement si nécessaire
    eventGateway.publish(
        new MaisonUpdatedEvent(
            updatedMaison.getId(),
            updatedMaison.getLot(),
            updatedMaison.getVille(),
            updatedMaison.getQuartier(),
            updatedMaison.getType(),
            updatedMaison.getAnneeConstruction()));

    return mapper.toMaisonResponseDTO(updatedMaison);
  }

  @Override
  public void deleteMaison(String id) {
    if (!maisonRepository.existsById(id)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + id);
    }
    maisonRepository.deleteById(id);
    // Publier un événement MaisonSupprimeeEvenement si nécessaire
    eventGateway.publish(new MaisonDeletedEvent(id));
  }

  // --- Opérations sur les Appartements ---
  @Override
  public AppartementResponseDTO addAppartementToMaison(
      String maisonId, AppartementRequestDTO appartementDTO) {
    Maison maison =
        maisonRepository
            .findById(maisonId)
            .orElseThrow(
                () -> new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId));

    // Mapper le DTO en entité
    Appartement appartement = mapper.toAppartement(appartementDTO);
    maison.addAppartement(appartement);

    // Persister l'appartement (grâce au cascade, la maison est aussi persistée)
    // appartementRepository.save(appartement);

    // La sauvegarde de la maison persiste aussi l'appartement
    maisonRepository.save(maison);

    eventGateway.publish(
        new AppartementAddedToMaisonEvent(
            appartement.getId(), appartement.getReference(), appartement.getReference()));

    // Mapper l'entité sauvegardée en DTO de réponse
    return mapper.toAppResponseDTO(appartement);
  }

  @Override
  public List<AppartementResponseDTO> findAppartementsByMaisonId(String maisonId) {
    if (!maisonRepository.existsById(maisonId)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId);
    }
    // Récupérer les appartements liés à la maison
    // et les mapper en DTOs de réponse
    List<Appartement> appartements = appartementRepository.findByMaisonId(maisonId);
    return mapper.toAppartementsResponseDTOs(appartements);
  }

  @Override
  public void removeAppartementFromMaison(String maisonId, String appartementId) {
    Maison maison =
        maisonRepository
            .findById(maisonId)
            .orElseThrow(
                () -> new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId));

    Appartement appartement =
        appartementRepository
            .findById(appartementId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Appartement non trouvé avec l'ID: " + appartementId)); // Exception
    // générique
    maison.removeAppartement(appartement);
    // appartementRepository.delete(appartement);
    maisonRepository.save(maison);

    // Publier un événement AppartementRetireDeMaisonEvent si nécessaire
    eventGateway.publish(new AppartementDeletedToMaisonEvent(appartement.getId(), maisonId));
  }

  @Override
  public AppartementResponseDTO updateAppartement(
      String maisonId, String appartementId, AppartementRequestDTO appartementDTO) {
    Maison maison =
        maisonRepository
            .findById(maisonId)
            .orElseThrow(
                () -> new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId));
    Appartement appartement =
        appartementRepository
            .findById(appartementId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Appartement non trouvé avec l'ID: " + appartementId)); // Exception
    // générique

    // Mapper le DTO en entité
    mapper.updateAppartementFromDto(appartementDTO, appartement);
    appartement.setMaison(maison);

    // Persister l'appartement
    appartementRepository.save(appartement);

    // Publier un événement AppartementMiseAJourEvenement si nécessaire
    eventGateway.publish(
        new AppartementUpdatedEvent(
            appartement.getId(),
            appartement.getReference(),
            maisonId,
            appartementId,
            appartement.getType().toString(),
            appartement.getNombreDePieces()));

    // Mapper l'entité sauvegardée en DTO de réponse
    return mapper.toAppResponseDTO(appartement);
  }

  @Override
  @Transactional(readOnly = true) // Optimisation pour les lectures
  public AppartementResponseDTO findAppartementById(String maisonId, String appartementId) {
    if (!maisonRepository.existsById(maisonId)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId);
    }
    Appartement appartement =
        appartementRepository
            .findById(appartementId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Appartement non trouvé avec l'ID: " + appartementId)); // Exception
    // générique

    return mapper.toAppResponseDTO(appartement);
  }

  @Override
  public void deleteAppartement(String maisonId, String id) {
    if (!maisonRepository.existsById(maisonId)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId);
    }
    if (!appartementRepository.existsById(id)) {
      throw new RuntimeException("Appartement non trouvé avec l'ID: " + id);
    }
    appartementRepository.deleteById(id);
    // Publier un événement AppartementSupprimeeEvenement si nécessaire
    eventGateway.publish(new AppartementDeletedToMaisonEvent(id, maisonId));
  }

  @Override
  public DepenseResponseDTO createDepense(DepenseRequestDTO depenseDTO) {
    // 1. Validation métier : une dépense doit être liée à un seul bien.
    validateDepenseAssociation(depenseDTO);

    // 2. Mapper et sauvegarder
    Depense depense = mapper.toDepense(depenseDTO);
    Depense savedDepense = depenseRepository.save(depense);

    // 3. Publier l'événement
    String bienId =
        savedDepense.getMaisonId() != null
            ? savedDepense.getMaisonId()
            : savedDepense.getAppartementId();
    String typeBien = savedDepense.getMaisonId() != null ? "MAISON" : "APPARTEMENT";

    eventGateway.publish(
        new DepenseRecordedEvent(
            savedDepense.getId(),
            bienId,
            typeBien,
            savedDepense.getMontant(),
            savedDepense.getDescription(),
            savedDepense.getDate()));

    return mapper.toDepenseResponseDTO(savedDepense);
  }

  @Override
  @Transactional(readOnly = true) // Optimisation pour les lectures
  public List<DepenseResponseDTO> findDepensesByMaisonId(String maisonId) {
    if (!maisonRepository.existsById(maisonId)) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + maisonId);
    }
    return mapper.toDepenseResponseDTOList(depenseRepository.findByMaisonId(maisonId));
  }

  @Override
  public List<DepenseResponseDTO> findDepensesByAppartementId(String appartementId) {
    if (!appartementRepository.existsById(appartementId)) {
      throw new MaisonNotFoundException("Appartement non trouvé avec l'ID: " + appartementId);
    }
    return mapper.toDepenseResponseDTOList(depenseRepository.findByAppartementId(appartementId));
  }

  @Override
  public DepenseResponseDTO updateDepense(String depenseId, DepenseRequestDTO requestDTO) {
    Depense depense =
        depenseRepository
            .findById(depenseId)
            .orElseThrow(
                () -> new MaisonNotFoundException("Dépense non trouvée avec l'ID: " + depenseId));

    // Validation de l'association
    validateDepenseAssociation(requestDTO);

    // Mettre à jour les champs
    depense.setMontant(requestDTO.montant());
    depense.setDescription(requestDTO.description());
    depense.setDate(requestDTO.date());
    depense.setMaisonId(requestDTO.maisonId());
    depense.setAppartementId(requestDTO.appartementId());

    Depense updatedDepense = depenseRepository.save(depense);

    // Publier un événement de mise à jour si nécessaire
    String bienId =
        updatedDepense.getMaisonId() != null
            ? updatedDepense.getMaisonId()
            : updatedDepense.getAppartementId();
    String typeBien = updatedDepense.getMaisonId() != null ? "MAISON" : "APPARTEMENT";

    eventGateway.publish(
        new DepenseUpdatedEvent(
            updatedDepense.getId(),
            bienId,
            typeBien,
            updatedDepense.getMontant(),
            updatedDepense.getDescription(),
            updatedDepense.getDate()));

    return mapper.toDepenseResponseDTO(updatedDepense);
  }

  @Override
  public void deleteDepense(String depenseId) {
    if (!depenseRepository.existsById(depenseId)) {
      throw new MaisonNotFoundException("Dépense non trouvée avec l'ID: " + depenseId);
    }
    depenseRepository.deleteById(depenseId);
    // Publier un événement de suppression si nécessaire
    eventGateway.publish(new DepenseDeletedEvent(depenseId));
  }

  // Méthode utilitaire pour valider l'association d'une dépense
  private void validateDepenseAssociation(DepenseRequestDTO dto) {
    if (dto.maisonId() != null && dto.appartementId() != null) {
      throw new IllegalArgumentException(
          "Une dépense ne peut pas être associée à la fois à une maison et un appartement.");
    }
    if (dto.maisonId() == null && dto.appartementId() == null) {
      throw new IllegalArgumentException(
          "Une dépense doit être associée à une maison ou un appartement.");
    }
    // Vérifier que le bien existe
    if (dto.maisonId() != null && !maisonRepository.existsById(dto.maisonId())) {
      throw new MaisonNotFoundException("Maison non trouvée avec l'ID: " + dto.maisonId());
    }
    if (dto.appartementId() != null && !appartementRepository.existsById(dto.appartementId())) {
      throw new MaisonNotFoundException("Appartement non trouvé avec l'ID: " + dto.appartementId());
    }
  }
}
