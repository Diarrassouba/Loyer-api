package ci.kossovo.financial_query_service.dtos;

import java.math.BigDecimal;
import java.time.YearMonth;

// Pour renvoyer la synthèse d'un compte financier d'un contrat
public record SyntheseFinanciereDTO(
    String contratId,
    String locataireId,
    String bienId,
    //Locataire locataire,
    BigDecimal solde,
    YearMonth dernierMoisGenere) {}
