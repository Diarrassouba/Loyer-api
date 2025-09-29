package ci.kossovo.immobilier_rest_api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Maison {

  @Id private String id = UUID.randomUUID().toString();

  @Column(nullable = false, length = 10)
  private String ilot;

  @Column(nullable = false, length = 10)
  private String quartier;

  @Column(nullable = false)
  private String ville;

  @Column(nullable = false)
  private int anneeConstruction;

  private String adresse;

  // Relation: Une maison a plusieurs appartements.
  // 'mappedBy="maison"' indique que l'entité Appartement gère la clé étrangère.
  // 'cascade = CascadeType.ALL' propage les opérations (save, delete) aux appartements liés.
  // 'orphanRemoval = true' supprime un appartement de la BDD s'il est retiré de cette liste.

  @OneToMany(
      mappedBy = "maison",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private List<Appartement> appartements = new ArrayList<>();

  public void addAppartement(Appartement appartement) {
    appartements.add(appartement);
    appartement.setMaison(this);
  }
}
