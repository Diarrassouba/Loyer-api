package ci.kossovo.financial_command_service.api;

import ci.kossovo.financial_command_service.dtos.PaiementRequestDTO;
import ci.kossovo.loyer_core_api.commands.financial.RecordPaymentCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/command/finance")
@Tag(name = "Financial Commands", description = "API pour les opérations d'écriture financière")
public class FinancialCommandController {

  private final CommandGateway commandGateway;

  public FinancialCommandController(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  @Operation(summary = "Enregistre un paiement pour un contrat")
  @PostMapping("/contrats/{contratId}/paiements")
  public CompletableFuture<ResponseEntity<String>> enregistrerPaiement(
      @PathVariable String contratId, @Valid @RequestBody PaiementRequestDTO paiementDTO) {

    UUID paiementId = UUID.randomUUID();
    RecordPaymentCommand command =
        new RecordPaymentCommand(
            contratId, paiementId, paiementDTO.montant(), paiementDTO.datePaiement(), null);

    // Envoi asynchrone de la commande
    return commandGateway
        .send(command)
        .thenApply(
            result ->
                ResponseEntity.accepted()
                    .body("Paiement " + paiementId + " accepté pour traitement."))
        .exceptionally(
            ex ->
                ResponseEntity.badRequest()
                    .body("Erreur lors du traitement du paiement: " + ex.getMessage()));
  }
}
