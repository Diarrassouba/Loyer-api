package ci.kossovo.raiting_service.projections;

import static org.assertj.core.api.Assertions.assertThat;

import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.LocataireCreatedEvent;
import ci.kossovo.raiting_service.projections.models.ContratValideView;
import ci.kossovo.raiting_service.projections.repositories.ContratValideRepository;
import ci.kossovo.raiting_service.projections.repositories.LocataireValideRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ValidationProjectionTest {

  @Autowired private ContratValideRepository contratValideRepository;

  @Autowired private LocataireValideRepository locataireValideRepository;

  private ValidationProjection projection;

  @BeforeEach
  void setUp() {
    // Instancier la projection avant chaque test
    // en lui passant les instances de repository injectées et gérées par le
    // contexte de test
    projection = new ValidationProjection(contratValideRepository, locataireValideRepository);
  }

  @Test
  @DisplayName("Quand un tenantCreatedEvent est reçu, un LocataireValideView doit être créé")
  void onLocataireCreeEvenement_shouldCreateLocataireValideView() {
    // --- ARRANGE ---
    String locataireId = "loc-123";
    LocataireCreatedEvent event =
        new LocataireCreatedEvent(locataireId, "Bamba", "Moussa", locataireId, locataireId);

    // --- ACT ---
    projection.on(event);

    // --- ASSERT ---
    // On interroge directement la base de données pour vérifier le résultat
    boolean exists = locataireValideRepository.existsById(locataireId);
    assertThat(exists).isTrue();

    // On vérifie qu'aucun contrat n'a été créé par erreur
    assertThat(contratValideRepository.count()).isZero();
  }

  @Test
  @DisplayName("Quand un ContratCreeEvenement est reçu, un ContratValideView doit être créé")
  void onContratCreeEvenement_shouldCreateContratValideView() {
    // --- ARRANGE ---
    String locataireId = "loc-456";
    String contratId = "contrat-789";
    BigDecimal montantLoyerMensuel = new BigDecimal("1000");
    BigDecimal montantAvance = new BigDecimal("2000");
    BigDecimal montantCaution = new BigDecimal("2000");

    projection = new ValidationProjection(contratValideRepository, locataireValideRepository);
    ContratCreatedEvent event =
        new ContratCreatedEvent(
            contratId,
            locataireId,
            "apt-1",
            "APPARTEMENT",
            montantLoyerMensuel,
            montantCaution,
            montantAvance,
            LocalDate.now());

    // --- ACT ---
    projection.on(event);

    // --- ASSERT ---
    ContratValideView savedView = contratValideRepository.findById(contratId).orElse(null);

    assertThat(savedView).isNotNull();
    assertThat(savedView.getContratId()).isEqualTo(contratId);
    assertThat(savedView.getLocataireId()).isEqualTo(locataireId);

    // On vérifie qu'aucun locataire n'a été créé par erreur
    assertThat(locataireValideRepository.count()).isZero();
  }

  @Test
  @DisplayName("Doit gérer une séquence d'événements pour plusieurs entités")
  void shouldHandleSequenceOfEvents() {
    BigDecimal montantLoyerMensuel = new BigDecimal("1000");
    BigDecimal montantAvance = new BigDecimal("2000");
    BigDecimal montantCaution = montantLoyerMensuel.multiply(BigDecimal.valueOf(2));

    projection = new ValidationProjection(contratValideRepository, locataireValideRepository);
    // --- ARRANGE ---
    // Événements pour le premier locataire et son contrat
    LocataireCreatedEvent locataire1Event =
        new LocataireCreatedEvent("loc-1", "Nom1", "Prenom1", "loc-1", "loc-1");
    ContratCreatedEvent contrat1Event =
        new ContratCreatedEvent(
            "contrat-1",
            "loc-1",
            "apt-1",
            "APPARTEMENT",
            montantLoyerMensuel,
            montantCaution,
            montantAvance,
            LocalDate.now());

    // Événements pour le second locataire
    LocataireCreatedEvent locataire2Event =
        new LocataireCreatedEvent("loc-2", "Nom2", "Prenom2", "loc-2", "loc-2");

    // --- ACT ---
    // Simuler l'arrivée des événements dans un certain ordre
    projection.on(locataire1Event);
    projection.on(contrat1Event);
    projection.on(locataire2Event);

    // --- ASSERT ---
    // Vérifier l'état final de la base de données
    assertThat(locataireValideRepository.count()).isEqualTo(2);
    assertThat(locataireValideRepository.existsById("loc-1")).isTrue();
    assertThat(locataireValideRepository.existsById("loc-2")).isTrue();

    assertThat(contratValideRepository.count()).isEqualTo(1);
    assertThat(contratValideRepository.existsById("contrat-1")).isTrue();
    assertThat(contratValideRepository.findById("contrat-1").get().getLocataireId())
        .isEqualTo("loc-1");
  }
}
