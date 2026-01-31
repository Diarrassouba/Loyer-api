package ci.kossovo.raiting_service.services.impl;

import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.dtos.NotationResponseDTO;
import ci.kossovo.raiting_service.mapper.RatingMapper;
import ci.kossovo.raiting_service.models.Notation;
import ci.kossovo.raiting_service.projections.repositories.ContratValideRepository;
import ci.kossovo.raiting_service.projections.repositories.LocataireValideRepository;
import ci.kossovo.raiting_service.repositories.NotationRepository;
import ci.kossovo.raiting_service.services.NotationService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotationServiceImpl implements NotationService {

  private final NotationRepository notationRepository;
  private final RatingMapper mapper;
  private final EventGateway eventGateway;

  private final ContratValideRepository contratValideRepository;
  private final LocataireValideRepository locataireValideRepository;

  public NotationServiceImpl(
      NotationRepository notationRepository,
      RatingMapper mapper,
      EventGateway eventGateway,
      ContratValideRepository contratValideRepository,
      LocataireValideRepository locataireValideRepository) {
    this.notationRepository = notationRepository;
    this.mapper = mapper;
    this.eventGateway = eventGateway;
    this.contratValideRepository = contratValideRepository;
    this.locataireValideRepository = locataireValideRepository;
  }

  @Override
  public NotationResponseDTO createNotation(CreerNotationRequest requestDTO) {

    // Logique métier de validation (si nécessaire, ex: vérifier que le contrat existe)
    validateIds(requestDTO);

    // Pour l'instant, on se fie à la validation du DTO.

    // 1. Mapper, compléter et sauvegarder
    Notation notation = mapper.toNotation(requestDTO);
    notation.setDateNotation(LocalDate.now());
    Notation savedNotation = notationRepository.save(notation);

    // 2. Préparer le DTO de réponse avec le score moyen
    NotationResponseDTO responseDTO = mapper.toNotationResponseDTO(savedNotation);

    // 3. Publier l'événement avec le score moyen
    eventGateway.publish(
        new TenantNoteEvent(
            responseDTO.id(),
            responseDTO.locataireId(),
            responseDTO.contratId(),
            responseDTO.scoreMoyen(), // Utiliser le score calculé par le mapper
            responseDTO.dateNotation()));

    return responseDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotationResponseDTO> findNotationsByLocataireId(String locataireId) {
    return mapper.toNotationResponseDTOList(notationRepository.findByLocataireId(locataireId));
  }

  @Override
  @Transactional(readOnly = true)
  public NotationResponseDTO findNotationById(String id) {
    return notationRepository
        .findById(id)
        .map(mapper::toNotationResponseDTO)
        .orElseThrow(() -> new EntityNotFoundException("Notation non trouvée avec l'ID: " + id));
  }

  /******************** Méthode privée pour encapsuler la logique de validation. ************************/
  private void validateIds(CreerNotationRequest dto) {
    // 1. Vérifier si le locataire existe
    if (!locataireValideRepository.existsById(dto.locataireId())) {
      throw new EntityNotFoundException("Locataire non trouvé avec l'ID: " + dto.locataireId());
    }

    // 2. Vérifier si le contrat existe et s'il est bien associé au bon locataire
    contratValideRepository
        .findById(dto.contratId())
        .map(
            contrat -> {
              if (!contrat.getLocataireId().equals(dto.locataireId())) {
                throw new IllegalArgumentException(
                    "Le contrat "
                        + dto.contratId()
                        + " n'est pas associé au locataire "
                        + dto.locataireId());
              }
              return contrat;
            })
        .orElseThrow(
            () -> new EntityNotFoundException("Contrat non trouvé avec l'ID: " + dto.contratId()));
  }
}
