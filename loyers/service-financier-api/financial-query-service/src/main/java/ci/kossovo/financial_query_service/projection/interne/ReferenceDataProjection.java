package ci.kossovo.financial_query_service.projection.interne;

import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;
import ci.kossovo.financial_query_service.projection.interne.model.LocataireViewDocument;
import ci.kossovo.financial_query_service.projection.interne.repository.BienImmobilierViewRepository;
import ci.kossovo.financial_query_service.projection.interne.repository.LocataireViewRepository;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementUpdatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonUpdatedEvent;
import ci.kossovo.loyer_core_api.events.locations.LocataireCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("query-finance-reference-data") // Nouveau groupe de traitement
public class ReferenceDataProjection {

  private final LocataireViewRepository locataireRepo;
  private final BienImmobilierViewRepository bienRepo;

  public ReferenceDataProjection(
      LocataireViewRepository locataireRepo, BienImmobilierViewRepository bienRepo) {
    this.locataireRepo = locataireRepo;
    this.bienRepo = bienRepo;
  }

  @EventHandler
  public void on(LocataireCreatedEvent evt) {
    String nomComplet = evt.prenom() + " " + evt.nom();

    // Formatage des infos de contact
    String contact = "";
    if (evt.telephone() != null) contact += evt.telephone();
    if (evt.email() != null) {
      if (!contact.isEmpty()) contact += " / ";
      contact += evt.email();
    }

    locataireRepo.save(new LocataireViewDocument(evt.id(), nomComplet, contact));
  }

  @EventHandler
  public void on(MaisonCreatedEvent evt) {
    String description = "Maison - " + evt.lot() + ", " + " " + evt.quartier() + ", " + evt.ville();
    bienRepo.save(new BienImmobilierViewDocument(evt.maisonId(), description, evt.type()));
  }

  @EventHandler
  public void on(AppartementAddedEvent evt) {
    // Idéalement, cet événement devrait aussi contenir l'adresse de la maison parente.
    // Si ce n'est pas le cas, on se contente de la référence de l'appartement.
    String description = "Appartement (" + evt.reference() + ") - " + evt.lot();
    // Si vous avez l'adresse : "Appartement (" + evt.reference() + ") - " + evt.adresseMaison()
    bienRepo.save(new BienImmobilierViewDocument(evt.appartementId(), description, evt.type()));
  }

  @EventHandler
  public void on(MaisonUpdatedEvent evt) {
    // On récupère le document existant pour mettre à jour sa description
    bienRepo
        .findById(evt.maisonId())
        .ifPresent(
            existingDoc -> {
              String description =
                  "Maison - " + evt.lot() + ", " + " " + evt.quartier() + ", " + evt.ville();
              existingDoc.setDescriptionComplete(description);
              existingDoc.setType(evt.type().toString());
              bienRepo.save(existingDoc);
            });
  }

  @EventHandler
  public void on(AppartementUpdatedEvent evt) {
    // Même logique que pour la maison : on met à jour la description de l'appartement
    bienRepo
        .findById(evt.appartementId())
        .ifPresent(
            existingDoc -> {
              String description = "Appartement (" + evt.reference() + ") - " + evt.lot();
              existingDoc.setDescriptionComplete(description);
              existingDoc.setType(evt.type().toString());
              bienRepo.save(existingDoc);
            });
  }
}
