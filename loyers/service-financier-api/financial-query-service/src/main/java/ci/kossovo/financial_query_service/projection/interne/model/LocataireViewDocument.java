package ci.kossovo.financial_query_service.projection.interne.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vue_locataires")
public class LocataireViewDocument {
  @Id 
  private String locataireId;
  private String nomComplet;
  private String contactInfo; // NOUVEAU : Ex: "0123456789 - drissa@email.com"
}
