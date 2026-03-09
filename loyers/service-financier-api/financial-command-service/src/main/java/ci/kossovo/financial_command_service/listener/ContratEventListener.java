package ci.kossovo.financial_command_service.listener;

import ci.kossovo.loyer_core_api.commands.financial.CloseFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.InitializeFinancialAccountCommand;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
            evt.contratId(), evt.locataireId(), evt.montantLoyerMensuel());

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

  @EventHandler
  public void on(ContratFinishedEvent evt) {
    System.out.println("EVENT LISTENER: ContratFinishedEvent reçu pour contrat " + evt.contratId());
    // Ici, vous pourriez envoyer une commande pour clôturer le compte financier associé, par
    // exemple :
    // commandGateway.send(new CloseFinancialAccountCommand(evt.contratId()));
    CloseFinancialAccountCommand cmd = new CloseFinancialAccountCommand(evt.contratId());
    commandGateway.send(cmd);
  }
}
