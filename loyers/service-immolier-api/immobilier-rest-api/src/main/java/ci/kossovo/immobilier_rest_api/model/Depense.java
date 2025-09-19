package ci.kossovo.immobilier_rest_api.model;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "depenses")
@Getter
@Setter
public class Depense {

  @Id private String id = UUID.randomUUID().toString();

  @Column(nullable = false)
  private BigDecimal montant;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private LocalDate date;

  @Enumerated(EnumType.STRING) // Stocke le nom de l'enum ("REPARATION") en BDD
  @Column(nullable = false)
  private TypeDepense typeDepense;

  // La dépense est liée soit à une maison, soit à un appartement.
  // Ces champs sont des clés étrangères "manuelles" car la relation est polymorphe.
  // On ne met pas de @ManyToOne ici pour garder la flexibilité.
  private String maisonId;

  private String appartementId;
}
