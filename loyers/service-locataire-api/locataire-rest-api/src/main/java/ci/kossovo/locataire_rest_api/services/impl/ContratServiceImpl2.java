/* package ci.kossovo.locataire_rest_api.services.impl;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireResponseDto;
import ci.kossovo.locataire_rest_api.mappers.TenancyMapper;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;
import ci.kossovo.locataire_rest_api.models.Locataire;
import ci.kossovo.locataire_rest_api.repositories.AppartementDispoRepository;
import ci.kossovo.locataire_rest_api.repositories.ContratRepository;
import ci.kossovo.locataire_rest_api.repositories.DisponibiliteBienRepository;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.locataire_rest_api.repositories.LocataireRepository;
import ci.kossovo.locataire_rest_api.services.ContratService;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import ci.kossovo.loyer_core_api.events.locations.LocataireCreatedEvent;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ContratServiceImpl2 implements ContratService {

  private final ContratRepository contratRepository;
  private final LocataireRepository locataireRepository;
  private final DisponibiliteBienRepository disponibiliteRepository;
  private final HistoriqueLocataireRepository historiqueRepository;

  private final AppartementDispoRepository appartementDispoRepo;
  private final HistoriqueLocataireRepository historiqueRepo;
  private final TenancyMapper mapper;
  private final EventGateway eventGateway;

  // Injection de toutes les dépendances nécessaires
  public ContratServiceImpl2(
      ContratRepository contratRepository,
      LocataireRepository locataireRepository,
      DisponibiliteBienRepository disponibiliteRepository,
      HistoriqueLocataireRepository historiqueRepository,
      AppartementDispoRepository appartementDispoRepo,
      HistoriqueLocataireRepository historiqueRepo,
      TenancyMapper mapper,
      EventGateway eventGateway) {
    this.contratRepository = contratRepository;
    this.locataireRepository = locataireRepository;
    this.disponibiliteRepository = disponibiliteRepository;
    this.historiqueRepository = historiqueRepository;
    this.appartementDispoRepo = appartementDispoRepo;
    this.historiqueRepo = historiqueRepo;
    this.mapper = mapper;
    this.eventGateway = eventGateway;
  }

  @Override
  public ContratResponseDTO createContrat(ContratRequestDTO contratDTO) {

    // --- VALIDATION MÉTIER COMPLEXE ---
    // 1. Valider qu'un seul ID de bien est fourni

    String bienId = validateAndGetBienId(contratDTO);

    // 2. Vérifier la disponibilité du bien (via notre projection locale)
    checkDisponibilite(bienId);

    // 3. Vérifier l'historique du locataire (via notre projection locale)
    checkHistoriqueLocataire(contratDTO.locataireId());

    // --- EXÉCUTION ---
    ContratLocation contrat = mapper.toContratLocation(contratDTO);
    ContratLocation savedContrat = contratRepository.save(contrat);

    // Publication de l'événement
    String typeBien = contratDTO.maisonId() != null ? "MAISON" : "APPARTEMENT";
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

    if (!contrat.isActif()) throw new IllegalStateException("Le contrat est déjà terminé.");

    contrat.setActif(false);
    contrat.setDateFin(LocalDate.now());
    ContratLocation savedContrat = contratRepository.save(contrat);

    String bienId =
        savedContrat.getMaisonId() != null
            ? savedContrat.getMaisonId()
            : savedContrat.getAppartementId();
    eventGateway.publish(
        new ContratFinishedEvent(
            savedContrat.getId(),
            savedContrat.getLocataireId(),
            bienId,
            savedContrat.getMontantLoyerBase(),
            savedContrat.getDateDebut(),
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

  // --- Implémentations pour Locataire et autres finders ---

  @Override
  public LocataireResponseDto createLocataire(LocataireRequestDTO locataireRequestDTO) {
    LocataireDTO locataire = mapper.toContratLocation(locataireRequestDTO);

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

    return mapper.toLocataireDTO(savedLocataire);
  }

  @Override
  public LocataireResponseDto findLocataireById(String id) {
    Locataire locataire =
        locataireRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Locataire non trouvé avec l'ID: " + id));
    return mapper.toLocataireDTO(locataire);
  }

  @Override
  public List<LocataireResponseDto> findAllLocataires() {
    List<Locataire> locataires = locataireRepository.findAll();
    return mapper.toLocataireDTOs(locataires);
  }

  @Override
  public LocataireResponseDto updateLocataire(String id, LocataireRequestDTO locataireRequestDto) {
    Locataire existingLocataire =
        locataireRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Locataire non trouvé avec l'ID: " + id));

    // Mettre à jour les champs pertinents
    existingLocataire.setNom(locataireRequestDto.nom());
    existingLocataire.setPrenom(locataireRequestDto.prenom());
    existingLocataire.setEmail(locataireRequestDto.email());
    existingLocataire.setTelephone(locataireRequestDto.telephone());

    Locataire updatedLocataire = locataireRepository.save(existingLocataire);
    return mapper.toLocataireDTO(updatedLocataire);
  }

  // --- Méthodes privées de validation ---
  private String validateAndGetBienId(ContratRequestDTO dto) {
    if (dto.maisonId() != null && dto.appartementId() != null) {
      throw new IllegalArgumentException(
          "Un contrat ne peut concerner qu'une maison OU un appartement, pas les deux.");
    }
    String bienId = dto.maisonId() != null ? dto.maisonId() : dto.appartementId();
    if (bienId == null) {
      throw new IllegalArgumentException("Un ID de maison ou d'appartement est requis.");
    }
    return bienId;
  }

  private void checkDisponibilite(String bienId) {
    disponibiliteRepository
        .findById(bienId)
        .ifPresent(
            bien -> {
              if (bien.getStatut() == DisponibiliteBien.Statut.LOUE) {
                throw new IllegalStateException("Le bien immobilier " + bienId + " est déjà loué.");
              }
            });
  }

  private void checkHistoriqueLocataire(String locataireId) {
    historiqueRepository
        .findById(locataireId)
        .ifPresent(
            historique -> {
              if (historique.getNombreRetardsPaiement() > 3) {
                throw new IllegalStateException(
                    "Le locataire a un historique de paiement insatisfaisant.");
              }
              if (historique.getScoreMoyenGeneral() > 0
                  && historique.getScoreMoyenGeneral() < 2.5) {
                throw new IllegalStateException(
                    "Le locataire a un score de comportement insuffisant.");
              }
            });
  }
}
 */
