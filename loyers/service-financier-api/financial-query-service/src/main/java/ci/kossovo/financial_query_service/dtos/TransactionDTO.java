package ci.kossovo.financial_query_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Pour renvoyer une ligne de l'historique
public record TransactionDTO(
    String transactionId,
    String contratId,
    String locataireId,
    // Contrat contrat, // Détails du contrat pour référence
    // Locataire locataire, // Détails du locataire pour référence
    LocalDateTime date,
    String description,
    String type, // "LOYER" ou "PAIEMENT"
    BigDecimal montant,
    BigDecimal soldeApresTransaction) {}
