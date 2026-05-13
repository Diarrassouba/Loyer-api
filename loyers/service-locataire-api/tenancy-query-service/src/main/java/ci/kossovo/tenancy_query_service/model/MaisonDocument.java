package ci.kossovo.tenancy_query_service.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "maisons_completes")
public class MaisonDocument {

    @Id
    private String maisonId;
    
    private boolean loueeEnEntier = false;
    private int totalAppartements = 0;
    private int appartementsLoues = 0;

    // Les appartements sont directement imbriqués dans le document Mongo
    private List<AppartementItem> appartements = new ArrayList<>();

    public MaisonDocument(String maisonId) {
        this.maisonId = maisonId;
    }

    public MaisonDocument ajouterAppartement(AppartementItem apt) {
        this.appartements.add(apt);
        this.totalAppartements++;
        return this;
    }

    public MaisonDocument marquerLoueeEnEntier(boolean estLouee) {
        this.loueeEnEntier = estLouee;
        return this;
    }

    public MaisonDocument modifierCompteurLocation(int variation) {
        this.appartementsLoues += variation;
        return this;
    }

    // Méthode utilitaire pour mettre à jour le statut d'un appartement spécifique
    public MaisonDocument changerStatutAppartement(String appartementId, boolean estLoue) {
        this.appartements.stream()
            .filter(apt -> apt.getAppartementId().equals(appartementId))
            .findFirst()
            .ifPresent(apt -> apt.setLoue(estLoue));
        return this;
    }
}
