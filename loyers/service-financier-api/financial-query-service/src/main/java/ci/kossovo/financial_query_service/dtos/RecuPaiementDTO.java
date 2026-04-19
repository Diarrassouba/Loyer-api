package ci.kossovo.financial_query_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecuPaiementDTO(
    String numeroRecu,
    LocalDate datePaiement,
    BigDecimal montantPaye,
    String montantEnLettres,

    // Infos Contrat
    String contratId,
    String statutCompte, // Ex: "À jour (0 FCFA)", "Reste à payer : 50000 FCFA"

    // Infos Locataire
    String nomLocataire,
    String contactLocataire,

    // Infos Bien
    String descriptionBien) {}
