package ci.kossovo.financial_command_service.projection;

import ci.kossovo.financial_command_service.projection.models.ContratActifView;
import ci.kossovo.financial_command_service.projection.repositories.ContratActifRepository;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountCloturedEvent;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountInitialisedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("financial-projections")
public class ContratActifProjection {

  private final ContratActifRepository repository;

  public ContratActifProjection(ContratActifRepository repository) {
    this.repository = repository;
  }

  /**
   * Quand un compte financier est initialisé, cela signifie qu'un nouveau contrat est devenu actif
   * du point de vue financier.
   */
  @EventHandler
  public void on(FinancialAccountInitialisedEvent evt) {
    ContratActifView view = new ContratActifView(evt.contratId(), evt.montantLoyerMensuel());
    repository.save(view);
  }

  /**
   * Quand un contrat est terminé, il faut le marquer comme inactif ou le supprimer de cette vue
   * pour que le scheduler ne génère plus de loyer pour lui.
   */
  @EventHandler
  public void on(FinancialAccountCloturedEvent evt) {
    repository
        .findById(evt.contratId())
        .ifPresent(
            view -> {
              // Option 1: Marquer comme inactif
              // view.setActif(false);
              // repository.save(view);

              // Option 2: Supprimer directement (plus simple pour le scheduler)
              repository.delete(view);
            });
  }
}
