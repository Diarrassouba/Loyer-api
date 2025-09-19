package ci.kossovo.immobilier_rest_api.model;

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
  private double surfaceMetresCarres;

  @Column(nullable = false)
  private int nombreSallesBains;

  // Relation inverse: Plusieurs appartements appartiennent à une maison.
  // FetchType.LAZY est crucial pour ne pas charger la maison inutilement.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "maison_id", nullable = false)
  private Maison maison;
}
