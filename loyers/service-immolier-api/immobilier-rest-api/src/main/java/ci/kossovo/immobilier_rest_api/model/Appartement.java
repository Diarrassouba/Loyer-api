package ci.kossovo.immobilier_rest_api.model;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Appartement {

  @Id private String id = UUID.randomUUID().toString();

  @Column(nullable = false)
  private String reference;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private double surface;

  private int etage;
  private int nombreDePieces;

  // Relation inverse: Plusieurs appartements appartiennent à une maison.
  // FetchType.LAZY est crucial pour ne pas charger la maison inutilement.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "maison_id", nullable = false)
  private Maison maison;
}
