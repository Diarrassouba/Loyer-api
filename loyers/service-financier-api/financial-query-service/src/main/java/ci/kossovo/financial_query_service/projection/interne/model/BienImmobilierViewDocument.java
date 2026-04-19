package ci.kossovo.financial_query_service.projection.interne.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vue_biens_immobiliers")
public class BienImmobilierViewDocument {
  @Id private String bienId;
  // Ex: "Maison - 123 Rue de la Paix, Paris" ou "Apt 4B - 123 Rue..."
  private String descriptionComplete;
  private String type; // Ex: "Maison", "Appartement", "Studio"
}
