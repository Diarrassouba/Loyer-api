package ci.kossovo.locataire_rest_api.projection;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;
import ci.kossovo.locataire_rest_api.repositories.DisponibiliteBienRepository;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;

@Component
public class DisponibiliteProjection {

     private final DisponibiliteBienRepository repository;

    public DisponibiliteProjection(DisponibiliteBienRepository repository) {
        this.repository = repository;
    }

    // Apprend l'existence d'une maison
    @EventHandler
    public void on(MaisonCreatedEvent evt) {
        repository.save(new DisponibiliteBien(evt.maisonId()));
    }


     // Apprend l'existence d'un appartement
    @EventHandler
    public void on(AppartementAddedEvent evt) {
        repository.save(new DisponibiliteBien(evt.appartementId()));
    }


     // Marque un bien comme LOUE
    @EventHandler
    public void on(ContratCreatedEvent evt) {
        repository.findById(evt.bienId()).ifPresent(bien -> {
            bien.setStatut(DisponibiliteBien.Statut.LOUE);
            bien.setContratIdActif(evt.contratId());
            repository.save(bien);
        });
    }

     // Marque un bien comme DISPONIBLE
    @EventHandler
    public void on(ContratFinishedEvent evt) {
        repository.findById(evt.bienId()).ifPresent(bien -> {
            // Vérification de sécurité : on ne libère que si c'est bien le bon contrat
            if (evt.contratId().equals(bien.getContratIdActif())) {
                bien.setStatut(DisponibiliteBien.Statut.DISPONIBLE);
                bien.setContratIdActif(null);
                repository.save(bien);
            }
        });
    }
}
