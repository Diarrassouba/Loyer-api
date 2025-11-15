package ci.kossovo.locataire_rest_api.projection;

import ci.kossovo.locataire_rest_api.models.HistoriqueLocataire;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ObservedLatePaymentEvent;
import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueLocataireProjection {

  private final HistoriqueLocataireRepository repository;

  public HistoriqueLocataireProjection(HistoriqueLocataireRepository repository) {
    this.repository = repository;
  }

  // Écoute son propre événement pour créer ou mettre à jour un enregistrement
  @EventHandler
  public void on(ContratCreatedEvent evt) {
    HistoriqueLocataire historiqueLocataire =
        repository.findById(evt.locataireId()).orElse(new HistoriqueLocataire(evt.locataireId()));

    historiqueLocataire.incrementerContrats();
    repository.save(historiqueLocataire);
  }

  // Écoute un événement du SERVICE FINANCIER
  @EventHandler
  public void on(ObservedLatePaymentEvent evt) {
    // Le locataireId doit être dans l'événement !
    if (evt.locataireId() == null) return;

    HistoriqueLocataire historiqueLocataire =
        repository.findById(evt.locataireId()).orElse(new HistoriqueLocataire(evt.locataireId()));

    historiqueLocataire.incrementerRetards();
    repository.save(historiqueLocataire);
  }

  @EventHandler
  public void on(TenantNoteEvent evt) {
    HistoriqueLocataire vue =
        repository.findById(evt.locataireId()).orElse(new HistoriqueLocataire(evt.locataireId()));

    vue.ajouterNotation(evt.scoreMoyen());
    repository.save(vue);
  }

  // On peut aussi écouter les paiements pour savoir si un locataire a rattrapé son retard
  @EventHandler
  public void on(PaymentReceivedEvent evt) {
    // Si le nouveau solde du contrat est positif ou nul, le locataire n'est plus en retard sur ce
    // contrat
    // Cette logique peut être affinée, mais c'est l'idée générale.
    // if (evt.nouveauSolde().compareTo(BigDecimal.ZERO) >= 0) {
    //     // ... mettre à jour le statut 'actuellementEnRetard'
    // }
  }
}
