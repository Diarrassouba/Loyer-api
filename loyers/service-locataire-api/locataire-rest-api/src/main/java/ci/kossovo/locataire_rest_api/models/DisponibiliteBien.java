package ci.kossovo.locataire_rest_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class DisponibiliteBien {

  @Id private String bienId;

  @Enumerated(EnumType.STRING)
  private Statut statut = Statut.DISPONIBLE;

  // Pour savoir quel contrat occupe le bien
  private String contratIdActif;
  private String parentId; // ID de la maison pour les appartements, null pour les maisons
  private String typeBien; // "MAISON" ou "APPARTEMENT"
  private String adresse; // Adresse complète du bien

  public DisponibiliteBien(String bienId) {
    this.bienId = bienId;
  }

  public enum Statut {
    DISPONIBLE,
    LOUE
  }
}
