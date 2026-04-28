package ci.kossovo.financial_command_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ci.kossovo.financial_command_service.dtos.PaiementRequestDTO;
import ci.kossovo.loyer_core_api.commands.financial.RecordPaymentCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public class FinancialCommandControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

    // CRUCIAL POUR LES TESTS D'INTÉGRATION :
    // On désactive la connexion à Axon Server (qui nécessiterait un conteneur Docker Axon Server).
    // Axon va utiliser un bus de commande local (en mémoire) par défaut, ce qui est parfait pour le
    // test.
    registry.add("axon.axonserver.enabled", () -> "false");
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  // On mocke le CommandGateway car on ne veut pas déclencher la vraie logique de l'agrégat
  // (qui nécessite que l'agrégat soit initialisé). On veut juste tester que le contrôleur
  // fait bien son travail de mapping HTTP -> Command.
  @MockitoBean private CommandGateway commandGateway;

  // --- TESTS ---

  @SuppressWarnings("null")
@Test
  @DisplayName("POST /api/command/finance/contrats/{id}/paiements - Succès (202 Accepted)")
  void enregistrerPaiement_shouldAcceptCommandAndReturn202() throws Exception {
    // Arrange
    String contratId = "contrat-test-123";
    PaiementRequestDTO requestDTO =
        new PaiementRequestDTO(new BigDecimal("150.00"), LocalDate.now());

    // Le contrôleur attend un CompletableFuture du Gateway. On simule un succès immédiat.
    when(commandGateway.send(any(RecordPaymentCommand.class)))
        .thenReturn(CompletableFuture.completedFuture("OK"));

    ArgumentCaptor<RecordPaymentCommand> commandCaptor =
        ArgumentCaptor.forClass(RecordPaymentCommand.class);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/command/finance/contrats/{contratId}/paiements", contratId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isAccepted()) // Vérifie le statut HTTP 202
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.containsString(
                        "accepté pour traitement"))); // Vérifie une partie du message texte
                                                      // retourné

    // Vérification que le Gateway a bien été appelé avec la bonne commande
    verify(commandGateway).send(commandCaptor.capture());
    RecordPaymentCommand capturedCommand = commandCaptor.getValue();

    assertThat(capturedCommand.contratId()).isEqualTo(contratId);
    assertThat(capturedCommand.montant()).isEqualByComparingTo("150.00");
    assertThat(capturedCommand.paiementId())
        .isNotNull(); // Vérifie que le contrôleur a bien généré un UUID
  }

  @SuppressWarnings("null")
@Test
  @DisplayName(
      "POST /api/command/finance/contrats/{id}/paiements - Échec de validation (400 Bad Request)")
  void enregistrerPaiement_shouldReturn400_whenMontantIsNegative() throws Exception {
    // Arrange
    String contratId = "contrat-test-123";
    // Un montant négatif viole l'annotation @Positive du DTO
    PaiementRequestDTO requestDTO =
        new PaiementRequestDTO(new BigDecimal("-50.00"), LocalDate.now());

    // Act & Assert
    mockMvc
        .perform(
            post("/api/command/finance/contrats/{contratId}/paiements", contratId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isBadRequest()) // Vérifie le statut 400 du GlobalExceptionHandler
        .andExpect(
            jsonPath("$.validationErrors.montant")
                .exists()); // Vérifie que l'erreur indique bien le champ "montant"

    // On s'assure que si la validation de la requête échoue, la commande n'est JAMAIS envoyée au
    // Gateway
    verify(commandGateway, org.mockito.Mockito.never()).send(any());
  }
}
