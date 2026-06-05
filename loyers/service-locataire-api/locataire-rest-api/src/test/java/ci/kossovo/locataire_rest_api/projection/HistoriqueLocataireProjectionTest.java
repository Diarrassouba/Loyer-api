package ci.kossovo.locataire_rest_api.projection;

import static org.assertj.core.api.Assertions.assertThat;

import ci.kossovo.locataire_rest_api.models.HistoriqueLocataire;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.loyer_core_api.events.financial.LatePaymentObservedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class HistoriqueLocataireProjectionTest {

  @Autowired private HistoriqueLocataireRepository historiqueRepository;

  private HistoriqueLocataireProjection projection;

  @Test
  @DisplayName("Doit construire l'historique d'un locataire à partir de divers événements")
  void shouldBuildLocataireHistoryFromEvents() {
    // --- ARRANGE ---
    projection = new HistoriqueLocataireProjection(historiqueRepository);

    String locataireId = "loc-525";
    BigDecimal montantLoyerMensuel = new BigDecimal("1000");
    BigDecimal montantAvance = new BigDecimal("2000");
    BigDecimal montantCaution =
        montantLoyerMensuel.multiply(BigDecimal.valueOf(2)); // Caution = 2 mois de loyer

    // Création des événements
    ContratCreatedEvent premierContrat =
        new ContratCreatedEvent(
            "c1",
            locataireId,
            "apt-1",
            "APPARTEMENT",
            BigDecimal.TEN,
            montantAvance,
            montantCaution,
            LocalDate.now());
    ContratCreatedEvent deuxiemeContrat =
        new ContratCreatedEvent(
            "c2",
            locataireId,
            "apt-2",
            "APPARTEMENT",
            BigDecimal.TEN,
            montantAvance,
            montantCaution,
            LocalDate.now());
    LatePaymentObservedEvent premierRetard =
        new LatePaymentObservedEvent("c1", locataireId, YearMonth.now(), BigDecimal.ONE);
    LatePaymentObservedEvent deuxiemeRetard =
        new LatePaymentObservedEvent("c2", locataireId, YearMonth.now(), BigDecimal.ONE);
    TenantNoteEvent premiereNote =
        new TenantNoteEvent("n1", locataireId, "c1", 4.5, LocalDate.now());
    TenantNoteEvent deuxiemeNote =
        new TenantNoteEvent("n2", locataireId, "c1", 3.5, LocalDate.now());

    // --- ACT & ASSERT - Étape 1 : Premier contrat ---
    projection.on(premierContrat);
    Optional<HistoriqueLocataire> viewOpt1 = historiqueRepository.findById(locataireId);
    assertThat(viewOpt1).isPresent();
    assertThat(viewOpt1.get().getNombreContratsTotal()).isEqualTo(1);

    // --- ACT & ASSERT - Étape 2 : Deuxième contrat ---
    projection.on(deuxiemeContrat);
    assertThat(historiqueRepository.findById(locataireId).get().getNombreContratsTotal())
        .isEqualTo(2);

    // --- ACT & ASSERT - Étape 3 : Deux retards de paiement ---
    projection.on(premierRetard);
    projection.on(deuxiemeRetard);
    Optional<HistoriqueLocataire> viewOpt3 = historiqueRepository.findById(locataireId);
    assertThat(viewOpt3.get().getNombreRetardsPaiement()).isEqualTo(2);
    assertThat(viewOpt3.get().isActuellementEnRetard()).isTrue();

    // --- ACT & ASSERT - Étape 4 : Deux notations ---
    projection.on(premiereNote);
    assertThat(historiqueRepository.findById(locataireId).get().getScoreMoyenGeneral())
        .isEqualTo(4.5);

    projection.on(deuxiemeNote);
    // Vérifier que la moyenne est correctement calculée : (4.5 * 1 + 3.5) / 2 = 4.0
    assertThat(historiqueRepository.findById(locataireId).get().getScoreMoyenGeneral())
        .isEqualTo(4.0);
    assertThat(historiqueRepository.findById(locataireId).get().getNombreDeNotations())
        .isEqualTo(2);
  }
}
