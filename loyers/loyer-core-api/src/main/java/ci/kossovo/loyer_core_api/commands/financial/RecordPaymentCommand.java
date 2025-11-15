package ci.kossovo.loyer_core_api.commands.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour enregistrer un paiement partiel ou total
public record RecordPaymentCommand(
    @TargetAggregateIdentifier String contratId,
    UUID paiementId,
    BigDecimal montant,
    LocalDate datePaiement,
    String referenceLoyerMensuel // Pour lier le paiement à un mois spécifique si besoin
    ) {}
