package ci.kossovo.locataire_rest_api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.mappers.TenancyMapper;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;
import ci.kossovo.locataire_rest_api.models.HistoriqueLocataire;
import ci.kossovo.locataire_rest_api.repositories.ContratRepository;
import ci.kossovo.locataire_rest_api.repositories.DisponibiliteBienRepository;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.locataire_rest_api.repositories.LocataireRepository;
import ci.kossovo.locataire_rest_api.services.impl.ContratServiceImpl;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContratServiceImplTest {

  @Mock private ContratRepository contratRepository;
  @Mock private DisponibiliteBienRepository disponibiliteRepository;
  @Mock private HistoriqueLocataireRepository historiqueRepository;
  @Mock private LocataireRepository locataireRepository;
  @Mock private TenancyMapper tenancyMapper;
  @Mock private EventGateway eventGateway;

  @InjectMocks private ContratServiceImpl contratService;

  @Test
  @DisplayName(
      "createContrat - Doit réussir si le bien est disponible et l'historique du locataire est bon")
  public void createContrat_shouldSucceed_whenAllChecksPass() {
    // Arrange
    String bienId = "apt-123";
    String locataireId = "loc-abc";
    ContratRequestDTO requestDTO =
        new ContratRequestDTO(LocalDate.now(), BigDecimal.valueOf(1000), locataireId, bienId, null);

    ContratLocation contratToSave = new ContratLocation();
    ContratLocation savedContrat = new ContratLocation();
    savedContrat.setId("contrat-xyz");

    // Simuler les vérifications : le bien est disponible, l'historique est vide (donc ok)
    when(disponibiliteRepository.findById(bienId))
        .thenReturn(Optional.of(new DisponibiliteBien(bienId)));
    when(historiqueRepository.findById(locataireId)).thenReturn(Optional.empty());
    when(tenancyMapper.toContratLocation(requestDTO)).thenReturn(contratToSave);
    when(contratRepository.save(contratToSave)).thenReturn(savedContrat);

    // Act
    contratService.createContrat(requestDTO);

    // Assert
    verify(contratRepository).save(contratToSave);
    verify(eventGateway).publish(any(ContratCreatedEvent.class));
  }

  @Test
  @DisplayName("createContrat - Doit échouer si le bien est déjà loué")
  public void createContrat_shouldFail_whenBienIsLoue() {
    // Arrange
    String bienId = "apt-123";
    ContratRequestDTO requestDTO =
        new ContratRequestDTO(LocalDate.now(), BigDecimal.valueOf(1000), "loc-abc", bienId, null);

    // Simuler la projection de disponibilité : le bien est LOUÉ
    DisponibiliteBien bienLoue = new DisponibiliteBien(bienId);
    bienLoue.setStatut(DisponibiliteBien.Statut.LOUE);
    when(disponibiliteRepository.findById(bienId)).thenReturn(Optional.of(bienLoue));

    // Act & Assert
    assertThatThrownBy(() -> contratService.createContrat(requestDTO))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("est déjà loué");

    verify(contratRepository, never()).save(any());
    verifyNoInteractions(eventGateway);
  }

  @Test
  @DisplayName(
      "createContrat - Doit échouer si l'historique du locataire est mauvais (trop de retards)")
  public void createContrat_shouldFail_whenLocataireHistoryIsBad() {
    // Arrange
    String locataireId = "loc-abc";
    ContratRequestDTO requestDTO =
        new ContratRequestDTO(
            LocalDate.now(), BigDecimal.valueOf(1000), locataireId, "apt-123", null);

    // Simuler la projection d'historique : le locataire a 5 retards
    HistoriqueLocataire mauvaisPayeur = new HistoriqueLocataire(locataireId);
    mauvaisPayeur.setNombreRetardsPaiement(5);
    when(historiqueRepository.findById(locataireId)).thenReturn(Optional.of(mauvaisPayeur));

    // Simuler la dispo ok pour ne pas interférer
    when(disponibiliteRepository.findById("apt-123"))
        .thenReturn(Optional.of(new DisponibiliteBien("apt-123")));

    // Act & Assert
    assertThatThrownBy(() -> contratService.createContrat(requestDTO))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("historique de paiement insatisfaisant");

    verify(contratRepository, never()).save(any());
  }
}
