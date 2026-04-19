package ci.kossovo.financial_query_service.projection.interne.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contrats")
@Data
public class Contrat {

  @Id String contratId;
  String locataireId;
  String bienId; // ID de la maison ou de l'appartement
  String typeBien; // "MAISON" ou "APPARTEMENT"
  BigDecimal montantLoyerMensuel;
  LocalDate dateDebut; // ...soit l'autre
}
