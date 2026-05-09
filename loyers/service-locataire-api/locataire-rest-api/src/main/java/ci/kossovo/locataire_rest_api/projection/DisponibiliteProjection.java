package ci.kossovo.locataire_rest_api.projection;

import ci.kossovo.locataire_rest_api.models.AppartementDispoView;
import ci.kossovo.locataire_rest_api.models.MaisonDispoView;
import ci.kossovo.locataire_rest_api.repositories.AppartementDispoRepository;
import ci.kossovo.locataire_rest_api.repositories.MaisonDispoRepository;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedToMaisonEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import java.util.Optional;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ProcessingGroup("tenancy-projections")
@Transactional // Assure que la mise à jour parent/enfant est atomique
public class DisponibiliteProjection {

  private final MaisonDispoRepository maisonRepo;
  private final AppartementDispoRepository aptRepo;

  // private final DisponibiliteBienRepository disponibiliteRepository;

  public DisponibiliteProjection(
      MaisonDispoRepository maisonRepo, AppartementDispoRepository aptRepo) {
    this.maisonRepo = maisonRepo;
    this.aptRepo = aptRepo;
  }

  // ===================================================================
  // 1. CRÉATION DES BIENS
  // ===================================================================

  @EventHandler
  public void on(MaisonCreatedEvent evt) {
    maisonRepo.save(new MaisonDispoView(evt.maisonId()));
  }

  @EventHandler
  public void on(AppartementAddedToMaisonEvent evt) {
    aptRepo.save(new AppartementDispoView(evt.appartementId(), evt.maisonId()));

    // Pipeline : Trouve la maison -> Incrémente le total -> Sauvegarde
    maisonRepo
        .findById(evt.maisonId())
        .map(this::incrementerTotalAppartements)
        .ifPresent(maisonRepo::save);
  }

  // ===================================================================
  // 2. LOCATION DES BIENS (Routage Fonctionnel)
  // ===================================================================

  @EventHandler
  public void on(ContratCreatedEvent evt) {
    // Routage pour MAISON
    Optional.of(evt)
        .filter(e -> "MAISON".equals(e.typeBien()))
        .ifPresent(e -> louerMaison(e.bienId()));

    // Routage pour APPARTEMENT
    Optional.of(evt)
        .filter(e -> "APPARTEMENT".equals(e.typeBien()))
        .ifPresent(e -> louerAppartement(e.bienId()));
  }

  // ===================================================================
  // 3. FIN DE LOCATION (Routage Fonctionnel)
  // ===================================================================

  @EventHandler
  public void on(ContratFinishedEvent evt) {
    // Routage pour MAISON
    Optional.of(evt)
        .filter(e -> "MAISON".equals(e.typeBien()))
        .ifPresent(e -> libererMaison(e.bienId()));

    // Routage pour APPARTEMENT
    Optional.of(evt)
        .filter(e -> "APPARTEMENT".equals(e.typeBien()))
        .ifPresent(e -> libererAppartement(e.bienId()));
  }

  // ===================================================================
  // MÉTHODES PRIVÉES : PIPELINES DE TRANSFORMATION DE DONNÉES
  // ===================================================================

  // --- Actions sur les Maisons ---

  private void louerMaison(String maisonId) {
    maisonRepo.findById(maisonId).map(this::marquerMaisonLoueeEnEntier).ifPresent(maisonRepo::save);
  }

  private void libererMaison(String maisonId) {
    maisonRepo.findById(maisonId).map(this::marquerMaisonLibre).ifPresent(maisonRepo::save);
  }

  // --- Actions sur les Appartements (Implique la mise à jour du Parent) ---

  private void louerAppartement(String appartementId) {
    // Pipeline : Trouve l'appart -> Le loue & Sauvegarde -> Récupère l'ID Maison parent
    // -> Trouve la Maison -> Incrémente ses apparts loués -> Sauvegarde la maison.
    aptRepo
        .findById(appartementId)
        .map(this::marquerAppartementLoue)
        .map(aptRepo::save)
        .map(AppartementDispoView::getMaisonId)
        .flatMap(maisonRepo::findById)
        .map(this::incrementerAppartementsLoues)
        .ifPresent(maisonRepo::save);
  }

  private void libererAppartement(String appartementId) {
    // Pipeline inverse
    aptRepo
        .findById(appartementId)
        .map(this::marquerAppartementLibre)
        .map(aptRepo::save)
        .map(AppartementDispoView::getMaisonId)
        .flatMap(maisonRepo::findById)
        .map(this::decrementerAppartementsLoues)
        .ifPresent(maisonRepo::save);
  }

  // --- Fonctions pures de modification d'état (Retournent l'objet modifié pour le chaînage) ---

  private MaisonDispoView marquerMaisonLoueeEnEntier(MaisonDispoView maison) {
    maison.setLoueeEnEntier(true);
    return maison;
  }

  private MaisonDispoView marquerMaisonLibre(MaisonDispoView maison) {
    maison.setLoueeEnEntier(false);
    return maison;
  }

  private AppartementDispoView marquerAppartementLoue(AppartementDispoView apt) {
    apt.setLoue(true);
    return apt;
  }

  private AppartementDispoView marquerAppartementLibre(AppartementDispoView apt) {
    apt.setLoue(false);
    return apt;
  }

  private MaisonDispoView incrementerTotalAppartements(MaisonDispoView maison) {
    maison.setTotalAppartements(maison.getTotalAppartements() + 1);
    return maison;
  }

  private MaisonDispoView incrementerAppartementsLoues(MaisonDispoView maison) {
    maison.setAppartementsLoues(maison.getAppartementsLoues() + 1);
    return maison;
  }

  private MaisonDispoView decrementerAppartementsLoues(MaisonDispoView maison) {
    maison.setAppartementsLoues(maison.getAppartementsLoues() - 1);
    return maison;
  }

  /*
  @EventHandler
  public void on(AppartementAddedEvent evt) {
    // A. Créer l'appartement avec son lien parent
    aptRepo.save(new AppartementDispoView(evt.appartementId(), evt.maisonId()));

    // B. Incrémenter le compteur total de la maison parente
    maisonRepo
        .findById(evt.maisonId())
        .ifPresent(
            maison -> {
              maison.setTotalAppartements(maison.getTotalAppartements() + 1);
              maisonRepo.save(maison);
            });
  } */

  /*
  @EventHandler
  public void on(ContratCreatedEvent evt) {
    if ("MAISON".equals(evt.typeBien())) {
      // Location d'une maison entière
      maisonRepo
          .findById(evt.bienId())
          .ifPresent(
              maison -> {
                maison.setLoueeEnEntier(true);
                maisonRepo.save(maison);
              });
    } else if ("APPARTEMENT".equals(evt.typeBien())) {
      // Location d'un appartement
      aptRepo
          .findById(evt.bienId())
          .ifPresent(
              apt -> {
                apt.setLoue(true);
                aptRepo.save(apt);

                // Incrémenter le compteur des loués sur la maison parente
                maisonRepo
                    .findById(apt.getMaisonId())
                    .ifPresent(
                        maison -> {
                          maison.setAppartementsLoues(maison.getAppartementsLoues() + 1);
                          maisonRepo.save(maison);
                        });
              });
    }
  } */

  // --- 3. FIN DE LOCATION ---
  /*
  @EventHandler
  public void on(ContratFinishedEvent evt) {
    // Même logique à l'envers : on décrémente ou on met à false
    if ("MAISON".equals(evt.typeBien())) {
      maisonRepo
          .findById(evt.bienId())
          .ifPresent(
              maison -> {
                maison.setLoueeEnEntier(false);
                maisonRepo.save(maison);
              });
    } else if ("APPARTEMENT".equals(evt.typeBien())) {
      aptRepo
          .findById(evt.bienId())
          .ifPresent(
              apt -> {
                apt.setLoue(false);
                aptRepo.save(apt);

                maisonRepo
                    .findById(apt.getMaisonId())
                    .ifPresent(
                        maison -> {
                          maison.setAppartementsLoues(maison.getAppartementsLoues() - 1);
                          maisonRepo.save(maison);
                        });
              });
    }
  } */

}
