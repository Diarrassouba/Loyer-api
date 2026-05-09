package ci.kossovo.locataire_rest_api.services.impl;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireResponseDto;
import ci.kossovo.locataire_rest_api.mappers.TenancyMapper;
import ci.kossovo.locataire_rest_api.models.AppartementDispoView;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.Locataire;
import ci.kossovo.locataire_rest_api.models.MaisonDispoView;
import ci.kossovo.locataire_rest_api.repositories.AppartementDispoRepository;
import ci.kossovo.locataire_rest_api.repositories.ContratRepository;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.locataire_rest_api.repositories.LocataireRepository;
import ci.kossovo.locataire_rest_api.repositories.MaisonDispoRepository;
import ci.kossovo.locataire_rest_api.services.ContratService;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import ci.kossovo.loyer_core_api.events.locations.LocataireCreatedEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContratServiceImpl implements ContratService {

  private final ContratRepository contratRepository;
  private final MaisonDispoRepository maisonDispoRepo;
  private final AppartementDispoRepository appartementDispoRepo;
  private final HistoriqueLocataireRepository historiqueRepo;
  private final LocataireRepository locataireRepository;
  private final TenancyMapper mapper;
  private final EventGateway eventGateway;

  public ContratServiceImpl(
      ContratRepository contratRepository,
      MaisonDispoRepository maisonDispoRepo,
      AppartementDispoRepository appartementDispoRepo,
      HistoriqueLocataireRepository historiqueRepo,
      LocataireRepository locataireRepository,
      TenancyMapper mapper,
      EventGateway eventGateway) {
    this.contratRepository = contratRepository;
    this.maisonDispoRepo = maisonDispoRepo;
    this.appartementDispoRepo = appartementDispoRepo;
    this.historiqueRepo = historiqueRepo;
    this.locataireRepository = locataireRepository;
    this.mapper = mapper;
    this.eventGateway = eventGateway;
  }

  @Override
  public ContratResponseDTO createContrat(ContratRequestDTO requestDTO) {

    // 1. Validations Métier (Exécutées via des pipelines fonctionnels)
    validerCoherenceIds(requestDTO);

    // Si maisonId est présent, lance la validation Maison
    Optional.ofNullable(requestDTO.maisonId()).ifPresent(this::validerDisponibiliteMaison);

    // Si appartementId est présent, lance la validation Appartement
    Optional.ofNullable(requestDTO.appartementId())
        .ifPresent(this::validerDisponibiliteAppartement);

    // Valide l'historique du locataire
    Optional.ofNullable(requestDTO.locataireId()).ifPresent(this::validerHistoriqueLocataire);

    // 2. Détermination des valeurs dynamiques (Sans if/else)
    String typeBien =
        Optional.ofNullable(requestDTO.maisonId()).map(id -> "MAISON").orElse("APPARTEMENT");

    String bienId = Optional.ofNullable(requestDTO.maisonId()).orElse(requestDTO.appartementId());

    // 3. Persistance
    ContratLocation contrat = mapper.toContratLocation(requestDTO);
    contrat.setTypeBien(typeBien);
    ContratLocation savedContrat = contratRepository.save(contrat);

    // 4. Publication de l'événement
    eventGateway.publish(
        new ContratCreatedEvent(
            savedContrat.getId(),
            savedContrat.getLocataireId(),
            bienId,
            typeBien,
            savedContrat.getMontantLoyerBase(),
            savedContrat.getDateDebut()));

    return mapper.toContratResponseDTO(savedContrat);
  }

  @Override
  public ContratResponseDTO terminerContrat(String id) {
    ContratLocation contrat =
        contratRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrat non trouvé: " + id));

    // Remplacement du if(!contrat.isActif()) par Optional.filter
    Optional.of(contrat)
        .filter(Predicate.not(ContratLocation::isActif))
        .ifPresent(
            c -> {
              throw new IllegalStateException("Le contrat est déjà terminé.");
            });

    contrat.setActif(false);
    contrat.setDateFin(LocalDate.now());
    ContratLocation savedContrat = contratRepository.save(contrat);

    String bienId =
        Optional.ofNullable(savedContrat.getMaisonId()).orElse(savedContrat.getAppartementId());

    eventGateway.publish(
        new ContratFinishedEvent(
            savedContrat.getId(),
            savedContrat.getLocataireId(),
            bienId,
            savedContrat.getTypeBien(),
            savedContrat.getDateFin()));

    return mapper.toContratResponseDTO(savedContrat);
  }

  @Override
  public ContratResponseDTO findContratById(String id) {
    ContratLocation contrat =
        contratRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrat non trouvé avec l'ID: " + id));
    return mapper.toContratResponseDTO(contrat);
  }

  @Override
  public LocataireResponseDto createLocataire(LocataireRequestDTO locataireRequestDTO) {
    Locataire locataire = mapper.toLocataire(locataireRequestDTO);

    // On pourrait publier un LocataireCreeEvent si d'autres services s'y
    // intéressaient

    Locataire savedLocataire = locataireRepository.save(locataire);
    eventGateway.publish(
        new LocataireCreatedEvent(
            savedLocataire.getId(),
            savedLocataire.getNom(),
            savedLocataire.getPrenom(),
            savedLocataire.getEmail(),
            savedLocataire.getTelephone()));

    return mapper.toLocataireResponseDto(savedLocataire);
  }

  @Override
  public LocataireResponseDto findLocataireById(String id) {
    Locataire locataire =
        locataireRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Locataire non trouvé: " + id));
    return mapper.toLocataireResponseDto(locataire);
  }

  @Override
  public List<LocataireResponseDto> findAllLocataires() {

    List<Locataire> locataires = locataireRepository.findAll();
    return mapper.toLocataireResponseDtoList(locataires);
  }

  @Override
  public LocataireResponseDto updateLocataire(String id, LocataireRequestDTO locataireDTO) {
    Locataire existingLocataire =
        locataireRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Locataire non trouvé: " + id));

    // MapStruct peut mettre à jour un objet existant en utilisant le même DTO

    Locataire updatedLocataire = mapper.toLocataire(locataireDTO, existingLocataire);

    Locataire savedLocataire = locataireRepository.save(updatedLocataire);
    return mapper.toLocataireResponseDto(savedLocataire);
  }

  @Override
  public List<MaisonDispoView> getMaisonAll() {

    return maisonDispoRepo.findAll();
  }

  // ===================================================================
  // MÉTHODES DE VALIDATION PRIVÉES (Pipelines Fonctionnels)
  // ===================================================================

  private void validerCoherenceIds(ContratRequestDTO dto) {
    // Rejeter si les DEUX IDs sont présents (maison ET appartement)
    Optional.ofNullable(dto.maisonId())
        .flatMap(mId -> Optional.ofNullable(dto.appartementId()))
        .ifPresent(
            both -> {
              throw new IllegalArgumentException(
                  "Un contrat ne peut concerner une maison ET un appartement.");
            });

    // Rejeter si AUCUN ID n'est présent
    Optional.ofNullable(dto.maisonId())
        .or(() -> Optional.ofNullable(dto.appartementId()))
        .orElseThrow(
            () -> new IllegalArgumentException("Un ID de maison ou d'appartement est requis."));
  }

  private void validerDisponibiliteMaison(String maisonId) {
    MaisonDispoView maison =
        maisonDispoRepo
            .findById(maisonId)
            .orElseThrow(() -> new EntityNotFoundException("Maison introuvable"));

    // Remplacement du "if (!maison.estDisponible...)"
    Optional.of(maison)
        .filter(Predicate.not(MaisonDispoView::estDisponiblePourLocationEntiere))
        .ifPresent(
            m -> {
              throw new IllegalStateException(
                  "Impossible. Maison déjà louée ou contenant des appartements occupés ("
                      + m.getAppartementsLoues()
                      + " loués).");
            });
  }

  private void validerDisponibiliteAppartement(String appartementId) {
    AppartementDispoView apt =
        appartementDispoRepo
            .findById(appartementId)
            .orElseThrow(() -> new EntityNotFoundException("Appartement introuvable"));

    // Remplacement du "if (apt.isLoue())"
    Optional.of(apt)
        .filter(AppartementDispoView::isLoue)
        .ifPresent(
            a -> {
              throw new IllegalStateException("Cet appartement est déjà loué.");
            });

    // Remplacement de la vérification du parent
    maisonDispoRepo
        .findById(apt.getMaisonId())
        .filter(MaisonDispoView::isLoueeEnEntier)
        .ifPresent(
            m -> {
              throw new IllegalStateException(
                  "La maison entière fait déjà l'objet d'un contrat global.");
            });
  }

  private void validerHistoriqueLocataire(String locataireId) {
    // Remplacement du "if (hist.getNombreRetards() >= 3)"
    historiqueRepo
        .findById(locataireId)
        .filter(hist -> hist.getNombreRetardsPaiement() >= 3)
        .ifPresent(
            hist -> {
              throw new IllegalStateException("Historique de paiement inacceptable.");
            });
  }
}
