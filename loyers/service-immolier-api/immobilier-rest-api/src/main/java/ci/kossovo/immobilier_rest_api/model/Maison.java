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
import lombok.Setter;

@Entity
@Getter
@Setter
public class Maison {

  @Id
  private String id = UUID.randomUUID().toString();

  @Column(nullable = false, length = 10)
  private String ILot;

  @Column(nullable = false)
  private String adresse;

  @Column(nullable = false)
  private String ville;

  @Column(nullable = false, length = 10)
  private String quartier;

  private int anneeConstruction;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "maison_id", nullable = false)
  private List<Appartement> appartements = new ArrayList<>();

  public void addAppartement(Appartement appartement) {
    appartements.add(appartement);
    appartement.setMaison(this);
  }
}
