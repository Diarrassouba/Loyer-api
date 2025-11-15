package ci.kossovo.loyer_core_api.commands.financial;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour générer le loyer dû pour un mois donné
// Sera typiquement envoyée par un scheduler le 1er du mois
public record GenererLoyerMensuelCommand(
    @TargetAggregateIdentifier String contratId,
    UUID loyerId, // ID unique pour cette instance de loyer
    YearMonth moisAnnee,
    BigDecimal montant) {}
