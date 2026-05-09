package ci.kossovo.financial_query_service.projection.interne;

import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;
import ci.kossovo.financial_query_service.projection.interne.model.LocataireViewDocument;
import ci.kossovo.financial_query_service.projection.interne.repository.BienImmobilierViewRepository;
import ci.kossovo.financial_query_service.projection.interne.repository.LocataireViewRepository;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedToMaisonEvent;
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
    // String description = "Maison - " + evt.lot() + ", " + " " + evt.quartier() + ", " +
    // evt.ville();
    String adresseComplete = evt.lot() + ", " + " " + evt.quartier() + ", " + evt.ville();
    bienRepo.save(
        new BienImmobilierViewDocument(
            evt.maisonId(),
            formatType(evt.typeBatiment()), // Une méthode utilitaire pour formater joliment
            null,
            adresseComplete));
  }

  @EventHandler
  public void on(AppartementAddedToMaisonEvent evt) {

    // Ex: Adresse = "Lot 4B, 12 Rue de la Paix"
    String adresseComplete = "Lot " + evt.reference();

    if (evt.adresseBatiment() != null) {
      adresseComplete += ", " + evt.adresseBatiment();
    }
    bienRepo.save(
        new BienImmobilierViewDocument(
            evt.appartementId(),
            formatType(evt.typeLot()),
            // Idéalement, il faudrait aussi récupérer le typeBatiment de  la maison parente.
            formatType(evt.typeBatiment()),
            adresseComplete));
  }

  @EventHandler
  public void on(MaisonUpdatedEvent evt) {
    // On récupère le document existant pour mettre à jour sa description
    bienRepo
        .findById(evt.maisonId())
        .ifPresent(
            existingDoc -> {
              String adresseComplete = evt.lot() + ", " + " " + evt.quartier() + ", " + evt.ville();
              existingDoc.setAdresseComplete(adresseComplete);
              existingDoc.setTypePrecis(formatType(evt.typeBatiment()));
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
              String adresseComplete = "Lot " + evt.reference();
              if (evt.adresseBatiment() != null) {
                adresseComplete += ", " + evt.adresseBatiment();
              }
              existingDoc.setAdresseComplete(adresseComplete);
              existingDoc.setTypePrecis(formatType(evt.typeBatiment()));
              bienRepo.save(existingDoc);
            });
  }

  // Méthode utilitaire pour un affichage propre (ex: "DEUX_PIECES" -> "Deux pièces")
  private String formatType(String enumType) {
    if (enumType == null) return "Non défini";
    String lower = enumType.toLowerCase().replace("_", " ");
    return lower.substring(0, 1).toUpperCase() + lower.substring(1);
  }
}
