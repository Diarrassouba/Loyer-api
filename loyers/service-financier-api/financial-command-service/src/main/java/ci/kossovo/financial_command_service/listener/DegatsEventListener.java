package ci.kossovo.financial_command_service.listener;

import ci.kossovo.loyer_core_api.commands.financial.FacturerDegatsCommand;
import ci.kossovo.loyer_core_api.events.raiting.DegatsConstatesEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("cmd-finance-listener-degats")
public class DegatsEventListener {

  private final CommandGateway commandGateway;

  public DegatsEventListener(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  @EventHandler
  public void on(DegatsConstatesEvent evt) {
    System.out.println(
        "FINANCE: Réception de dégâts constatés pour le contrat "
            + evt.contratId()
            + " d'un montant de "
            + evt.montant()
            + " FCFA");

    // On traduit l'événement en commande de facturation de dégâts
    FacturerDegatsCommand command =
        new FacturerDegatsCommand(evt.contratId(), evt.montant(), evt.description());

    // Envoi à l'Agrégat
    commandGateway.send(command);
  }
}
