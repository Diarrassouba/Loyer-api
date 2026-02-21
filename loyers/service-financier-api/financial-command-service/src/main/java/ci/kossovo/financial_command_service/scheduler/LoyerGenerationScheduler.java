package ci.kossovo.financial_command_service.scheduler;

import ci.kossovo.financial_command_service.projection.models.ContratActifView;
import ci.kossovo.financial_command_service.projection.repositories.ContratActifRepository;
import ci.kossovo.loyer_core_api.commands.financial.GenerateMonthlyRentCommand;
import java.time.YearMonth;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoyerGenerationScheduler {

  private final CommandGateway commandGateway;
  private final ContratActifRepository contratActifRepository;

  public LoyerGenerationScheduler(
      CommandGateway commandGateway, ContratActifRepository contratActifRepository) {
    this.commandGateway = commandGateway;
    this.contratActifRepository = contratActifRepository;
  }

  @Scheduled(cron = "0 0 1 1 * *") // Le 1er de chaque mois à 1h du matin
  public void genererLoyersDuMois() {
    System.out.println("SCHEDULER: Démarrage de la génération des loyers pour " + YearMonth.now());

    // On lit la liste des contrats depuis notre projection locale
    Iterable<ContratActifView> contratsActifs = contratActifRepository.findAll();

    for (ContratActifView contrat : contratsActifs) {
      GenerateMonthlyRentCommand command =
          new GenerateMonthlyRentCommand(
              contrat.getContratId(),
              UUID.randomUUID(),
              YearMonth.now(),
              contrat.getMontantLoyer());

      commandGateway
          .send(command)
          .exceptionally(
              throwable -> {
                System.err.println(
                    "Erreur lors de la génération du loyer pour le contrat "
                        + contrat.getContratId()
                        + ": "
                        + throwable.getMessage());
                return null;
              });
    }
    System.out.println("SCHEDULER: Fin de la génération des loyers.");
  }
}
