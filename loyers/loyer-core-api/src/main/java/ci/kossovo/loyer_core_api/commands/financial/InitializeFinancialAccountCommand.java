package ci.kossovo.loyer_core_api.commands.financial;

import java.math.BigDecimal;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour initialiser le compte financier lorsqu'un contrat est créé
public record InitializeFinancialAccountCommand(
    @TargetAggregateIdentifier // Lie cette commande à une instance spécifique de l'agrégat
        String contratId,
    String locataireId,
    String bienId,
    BigDecimal montantLoyerMensuel) {}
