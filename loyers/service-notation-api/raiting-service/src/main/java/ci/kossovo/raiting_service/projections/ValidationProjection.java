package ci.kossovo.raiting_service.projections;

import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.LocataireCreatedEvent;
import ci.kossovo.raiting_service.projections.models.ContratValideView;
import ci.kossovo.raiting_service.projections.models.LocataireValideView;
import ci.kossovo.raiting_service.projections.repositories.ContratValideRepository;
import ci.kossovo.raiting_service.projections.repositories.LocataireValideRepository;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("raiting-projections")
public class ValidationProjection {

  private final ContratValideRepository contratRepository;
  private final LocataireValideRepository locataireRepository;

  public ValidationProjection(
      ContratValideRepository contratRepository, LocataireValideRepository locataireRepository) {
    this.contratRepository = contratRepository;
    this.locataireRepository = locataireRepository;
  }

  @EventHandler
  public void on(LocataireCreatedEvent evt) {
    locataireRepository.save(new LocataireValideView(evt.id()));
  }

  @EventHandler
  public void on(ContratCreatedEvent evt) {
    contratRepository.save(new ContratValideView(evt.contratId(), evt.locataireId()));
  }

  // On pourrait aussi écouter ContratFinishedEvent pour supprimer l'entrée,
  // mais la garder permet de noter un locataire même après la fin de son contrat.
}
