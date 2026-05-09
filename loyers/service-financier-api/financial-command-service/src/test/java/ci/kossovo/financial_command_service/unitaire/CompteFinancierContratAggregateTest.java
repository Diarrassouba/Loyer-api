package ci.kossovo.financial_command_service.unitaire;

import ci.kossovo.financial_command_service.aggregate.CompteFinancierContratAggregate;
import ci.kossovo.loyer_core_api.commands.financial.GenerateMonthlyRentCommand;
import ci.kossovo.loyer_core_api.commands.financial.InitializeFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.RecordPaymentCommand;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountInitialisedEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CompteFinancierContratAggregateTest {

  private FixtureConfiguration<CompteFinancierContratAggregate> fixture;

  @BeforeEach
  void setUp() {
    // Initialiser le "banc de test" pour notre agrégat
    fixture = new AggregateTestFixture<>(CompteFinancierContratAggregate.class);
  }

  @Test
  @DisplayName("Doit créer un compte financier et publier un événement d'initialisation")
  void shouldCreateAccountOnInitializationCommand() {
    String contratId = "contrat-1";
    String locataireId = "loc-1";
    String bienId = "bien-1";

    fixture
        .givenNoPriorActivity() // État initial : aucun événement passé
        .when(
            new InitializeFinancialAccountCommand(
                contratId,
                locataireId,
                bienId,
                new BigDecimal("1000"))) // Quand cette commande arrive...
        .expectSuccessfulHandlerExecution() // ... s'attendre à ce qu'elle soit gérée sans erreur
        .expectEvents(
            new FinancialAccountInitialisedEvent(
                contratId,
                locataireId,
                bienId,
                new BigDecimal("1000"))); // ... et que cet événement soit publié.
  }

  @Test
  @DisplayName("Doit initialiser le compte et publier l'événement correspondant")
  void shouldInitializeAccount() {
    String contratId = "contrat-123";
    String locataireId = "loc-456";
    String bienId = "bien-789";

    fixture
        .givenNoPriorActivity()
        .when(
            new InitializeFinancialAccountCommand(
                contratId, locataireId, bienId, new BigDecimal("800")))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new FinancialAccountInitialisedEvent(
                contratId, locataireId, bienId, new BigDecimal("800")));
  }

  @Test
  @DisplayName("Doit rejeter l'initialisation si le loyer de base est négatif")
  void shouldRejectInitializationIfRentIsNegative() {
    fixture
        .givenNoPriorActivity()
        .when(new InitializeFinancialAccountCommand("c1", "l1", "b1", new BigDecimal("-100")))
        .expectException(IllegalArgumentException.class)
        .expectExceptionMessage("Le montant du loyer doit être positif.");
  }

  @Test
  @DisplayName("Doit générer un loyer mensuel et mettre à jour le solde")
  void shouldGenerateMonthlyRentAndUpdateBalance() {
    String contratId = "contrat-1";
    String locataireId = "loc-1";
    String bienId = "bien-1";
    UUID loyerId = UUID.randomUUID();

    // Given : l'agrégat a déjà été initialisé
    fixture
        .given(
            new FinancialAccountInitialisedEvent(
                contratId, locataireId, bienId, new BigDecimal("700")))
        // When : on envoie une commande pour générer le loyer du mois
        .when(
            new GenerateMonthlyRentCommand(
                contratId, loyerId, YearMonth.now(), new BigDecimal("700")))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new RentMonthlyGeneredEvent(
                contratId, loyerId, locataireId, YearMonth.now(), new BigDecimal("700"), new BigDecimal("-700")));
  }

  @Test
  @DisplayName("Doit enregistrer un paiement et mettre à jour le solde")
  public void shouldRecordPaymentAndUpdateBalance() {
    String contratId = "contrat-1";
    String locataireId = "loc-1";
    String bienId = "bien-1";
    UUID paiementId = UUID.randomUUID();

    fixture
        .given(
            new FinancialAccountInitialisedEvent(
                contratId, locataireId, bienId, new BigDecimal("700")),
            new RentMonthlyGeneredEvent(
                contratId, UUID.randomUUID(), locataireId, YearMonth.now(), new BigDecimal("700"), new BigDecimal("-700")))
        .when(
            new RecordPaymentCommand(
                contratId, paiementId, new BigDecimal("500"), LocalDate.now(), null))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new PaymentReceivedEvent(
                contratId,
                locataireId,
                paiementId,
                new BigDecimal("500"),
                LocalDate.now(),
                new BigDecimal("-700"),
                new BigDecimal("-200")));
  }

  @Test
  @DisplayName("Doit rejeter un paiement avec un montant négatif")
  void shouldRejectPaymentWithNegativeAmount() {
    String contratId = "contrat-1";

    fixture
        .given(
            new FinancialAccountInitialisedEvent(
                contratId, "loc-1", "bien-1", new BigDecimal("700")))
        .when(
            new RecordPaymentCommand(
                contratId, UUID.randomUUID(), new BigDecimal("-100"), LocalDate.now(), null))
        .expectException(
            IllegalArgumentException.class) // S'attendre à ce qu'une exception soit levée
        .expectNoEvents(); // ... et qu'aucun événement ne soit publié.
  }
}
