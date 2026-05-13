package ci.kossovo.tenancy_query_service.projection;

import java.util.Optional;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedToMaisonEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import ci.kossovo.tenancy_query_service.model.AppartementItem;
import ci.kossovo.tenancy_query_service.model.MaisonDocument;
import ci.kossovo.tenancy_query_service.repository.MaisonDocumentRepository;

@Component
@ProcessingGroup("tenancy-views")
public class MaisonCompleteProjection {

    private final MaisonDocumentRepository maisonRepo;

    public MaisonCompleteProjection(MaisonDocumentRepository maisonRepo) {
        this.maisonRepo = maisonRepo;
    }

    // ===================================================================
    // 1. CRÉATION
    // ===================================================================

    @EventHandler
    public void on(MaisonCreatedEvent evt) {
        maisonRepo.save(new MaisonDocument(evt.maisonId()));
    }

    @EventHandler
    public void on(AppartementAddedToMaisonEvent evt) {
        AppartementItem nouvelApt = new AppartementItem(evt.appartementId(), evt.reference());
        
        maisonRepo.findById(evt.maisonId())
                .map(maison -> maison.ajouterAppartement(nouvelApt))
                .ifPresent(maisonRepo::save);
    }

    // ===================================================================
    // 2. LOCATIONS
    // ===================================================================

    @EventHandler
    public void on(ContratCreatedEvent evt) {
        Optional.of(evt)
                .filter(e -> "MAISON".equals(e.typeBien()))
                .flatMap(e -> maisonRepo.findById(e.bienId()))
                .map(m -> m.marquerLoueeEnEntier(true))
                .ifPresent(maisonRepo::save);

        Optional.of(evt)
                .filter(e -> "APPARTEMENT".equals(e.typeBien()))
                // On utilise notre méthode custom du repository pour trouver le document parent
                .flatMap(e -> maisonRepo.findByAppartementsAppartementId(e.bienId()))
                .map(maison -> maison.changerStatutAppartement(evt.bienId(), true))
                .map(maison -> maison.modifierCompteurLocation(1))
                .ifPresent(maisonRepo::save);
    }

    // ===================================================================
    // 3. FINS DE LOCATION
    // ===================================================================

    @EventHandler
    public void on(ContratFinishedEvent evt) {
        Optional.of(evt)
                .filter(e -> "MAISON".equals(e.typeBien()))
                .flatMap(e -> maisonRepo.findById(e.bienId()))
                .map(m -> m.marquerLoueeEnEntier(false))
                .ifPresent(maisonRepo::save);

        Optional.of(evt)
                .filter(e -> "APPARTEMENT".equals(e.typeBien()))
                .flatMap(e -> maisonRepo.findByAppartementsAppartementId(e.bienId()))
                .map(maison -> maison.changerStatutAppartement(evt.bienId(), false))
                .map(maison -> maison.modifierCompteurLocation(-1))
                .ifPresent(maisonRepo::save);
    }
}