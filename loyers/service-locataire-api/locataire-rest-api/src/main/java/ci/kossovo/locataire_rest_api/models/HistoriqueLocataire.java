package ci.kossovo.locataire_rest_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class HistoriqueLocataire {

    @Id
    private String locataireId;

    private int nombreContratsTotal = 0;
    private int nombreRetardsPaiement = 0;
    private boolean actuellementEnRetard = false;

    // NOUVEAUX CHAMPS POUR LES NOTATIONS
    private double scoreMoyenGeneral = 0.0;
    private int nombreDeNotations = 0;

    public HistoriqueLocataire(String locataireId) {
        this.locataireId = locataireId;
    }

    public void incrementerContrats() {
        this.nombreContratsTotal++;
    }

    public void incrementerRetards() {
        this.nombreRetardsPaiement++;
        this.actuellementEnRetard = true;
    }

    // Méthode pour mettre à jour le score
    public void ajouterNotation(double nouveauScore) {
        // Calcul de la moyenne mobile
        double sommeTotale = (this.scoreMoyenGeneral * this.nombreDeNotations) + nouveauScore;
        this.nombreDeNotations++;
        this.scoreMoyenGeneral = sommeTotale / this.nombreDeNotations;
    }

}
