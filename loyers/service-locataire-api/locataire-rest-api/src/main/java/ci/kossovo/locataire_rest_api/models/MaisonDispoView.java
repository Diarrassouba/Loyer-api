package ci.kossovo.locataire_rest_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class MaisonDispoView {

  @Id private String maisonId;

  // Pour savoir si la maison entière a été louée par un seul contrat global
  private boolean loueeEnEntier = false;

  // Statistiques des appartements enfants
  private int totalAppartements = 0;
  private int appartementsLoues = 0;

  public MaisonDispoView(String maisonId) {
    this.maisonId = maisonId;
  }

  // Règle métier : On ne peut louer une maison entière QUE si aucun de ses
  // appartements n'est actuellement loué, et qu'elle n'est pas déjà louée.
  public boolean estDisponiblePourLocationEntiere() {
    return !loueeEnEntier && appartementsLoues == 0;
  }
}
