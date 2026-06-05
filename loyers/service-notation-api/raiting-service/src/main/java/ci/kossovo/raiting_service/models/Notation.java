package ci.kossovo.raiting_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Entity
@Data
public class Notation {

  @Id private String id = UUID.randomUUID().toString();

  // Identifiants externes pour lier la notation au contexte
  @Column(nullable = false)
  private String locataireId;

  @Column(nullable = false)
  private String contratId;

  @Column(nullable = false)
  private LocalDate dateNotation;

  // Critères de notation (de 1 à 5)
  private int scoreProprete; // Propreté de l'appartement rendu
  private int scoreCommunication; // Facilité de communication
  private int scoreRespectVoisinage;
  private int scoreRespectReglement;

  @Lob // Pour les commentaires potentiellement longs
  private String commentaire;

  private String notePar; // ID du gestionnaire qui a noté

  // Dans Notation.java, on ajoute ces deux champs :
  private BigDecimal coutReparations = BigDecimal.ZERO; // Coût des dégâts
  private String descriptionDegats; // Description des dégradations
}
