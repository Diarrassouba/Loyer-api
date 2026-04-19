package ci.kossovo.financial_command_service.unitaire;

import ci.kossovo.financial_command_service.saga.SoldeContratSaga;
import ci.kossovo.loyer_core_api.events.financial.PaymentReminderRequiredEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SoldeContratSagaTest {

  private SagaTestFixture<SoldeContratSaga> fixture;

  @BeforeEach
  void setUp() {
    fixture = new SagaTestFixture<>(SoldeContratSaga.class);
  }

  @Test
  @DisplayName("Doit démarrer et planifier une vérification à la création d'un contrat")
  void shouldStartAndScheduleCheckOnContratCreation() {
    fixture
        .givenNoPriorActivity()
        .whenPublishingA(
            new ContratCreatedEvent("c1", "loc1", "apt1", "APT", BigDecimal.TEN, LocalDate.now()))
        .expectActiveSagas(1)
        .expectScheduledDeadline(Duration.ofDays(7), "verificationSolde");
  }

  @Test
  @DisplayName("Doit démarrer et planifier une deadline à la création du contrat")
  void shouldStartAndScheduleDeadlineOnContratCreation() {
    fixture
        .givenNoPriorActivity()
        .whenPublishingA(
            new ContratCreatedEvent(
                "contrat-1", "loc-1", "apt-1", "APPARTEMENT", BigDecimal.TEN, LocalDate.now()))
        .expectActiveSagas(1) // Vérifie que la saga est bien vivante
        .expectScheduledDeadline(
            Duration.ofDays(7), "verificationSolde"); // Vérifie la planification
  }

  @Test
  @DisplayName("Doit publier un événement de retard si le solde est négatif après la deadline")
  void shouldPublishLatePaymentEventWhenBalanceIsNegativeAfterDeadline() {
    String contratId = "c1";
    String locataireId = "loc1";

    fixture
        .givenAPublished(
            new ContratCreatedEvent(
                contratId, locataireId, "apt1", "APT", BigDecimal.TEN, LocalDate.now()))
        .andThenAPublished(
            new RentMonthlyGeneredEvent(
                contratId, UUID.randomUUID(), locataireId, YearMonth.now(), new BigDecimal("500")))
        // Avancer le temps de 8 jours pour dépasser la deadline de 7 jours
        .whenTimeElapses(Duration.ofDays(8))
        .expectActiveSagas(1)
        .expectDispatchedCommands(
            new PaymentReminderRequiredEvent(
                contratId, locataireId, new BigDecimal("500"))); // 8 jours de retard
  }
}
