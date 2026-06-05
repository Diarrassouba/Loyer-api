package ci.kossovo.financial_command_service.listener;

import ci.kossovo.loyer_core_api.commands.financial.FacturerDegatsCommand;
import ci.kossovo.loyer_core_api.commands.financial.InitializeFinancialAccountCommand;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.raiting.DegatsConstatesEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("cmd-finance-listener-init")
public class ContratEventListener {

  private static final Logger log = LoggerFactory.getLogger(ContratEventListener.class);
  private final CommandGateway commandGateway;

  public ContratEventListener(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  /**
   * Écoute l'événement de création de contrat publié par le tenancy-service. Traduit cet événement
   * en une commande pour créer un nouvel agrégat CompteFinancierContratAggregate.
   */
  @EventHandler
  public void on(ContratCreatedEvent evt) {

    // 1. LOG CRUCIAL : L'événement arrive-t-il ici ?
    log.info("===============");
    log.info("1. ÉVÉNEMENT REÇU : ContratCreeEvenement pour contrat [{}]", evt.contratId());
    log.info("===============");
    System.out.println("EVENT LISTENER: ContratCreatedEvent reçu pour contrat " + evt.contratId());

    InitializeFinancialAccountCommand cmd =
        new InitializeFinancialAccountCommand(
            evt.contratId(),
            evt.locataireId(),
            evt.bienId(),
            evt.montantLoyerMensuel(),
            evt.montantCaution(),
            evt.montantAvance());

    // Envoie la commande pour créer une nouvelle instance de l'agrégat.
    // 2. LOG CRUCIAL : Que se passe-t-il quand on envoie la commande ?
    commandGateway
        .send(cmd)
        .whenComplete(
            (result, exception) -> {
              if (exception != null) {
                log.error(
                    "!!! 2. ÉCHEC DE LA COMMANDE D'INITIALISATION pour contrat [{}] !!!",
                    evt.contratId(),
                    exception);
              } else {
                log.info("2. SUCCÈS : Agrégat initialisé pour contrat [{}]", evt.contratId());
              }
            });
  }

  /**
   * GESTIONNAIRE 2 : Écoute les dégradations lors de l'état des lieux (la fin du bail). Déclenche
   * la facturation automatique des réparations sur la caution.
   */
  @EventHandler
  public void on(DegatsConstatesEvent evt) {
    System.out.println(
        "FINANCE LISTENER: DegatsConstatesEvent reçu pour le contrat: "
            + evt.contratId()
            + " d'un montant de "
            + evt.montant()
            + " FCFA");

    commandGateway.send(
        new FacturerDegatsCommand(evt.contratId(), evt.montant(), evt.description()));
  }
}
