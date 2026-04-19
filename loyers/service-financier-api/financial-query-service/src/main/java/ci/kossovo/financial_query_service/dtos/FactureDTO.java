package ci.kossovo.financial_query_service.dtos;

import ci.kossovo.financial_query_service.projection.interne.model.Contrat;

public record FactureDTO(
    String numeroFacture,
    Contrat contrat,
    String nomCompletLocataire,
    String emailLocataire,
    String telephoneLocataire,
    double montantPaye,
    String datePaiement,
    double soldeAvantPaiement,
    double soldeApresPaiement,
    String montantLettres,
    String statut // Ex: "À jour", "Reste à payer : X"
    ) {}
