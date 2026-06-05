package ci.kossovo.loyer_core_api.commands.financial;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour restituer la caution d'un contrat. (ReturnDepositCommand)
public record RestituerCautionCommand(@TargetAggregateIdentifier String contratId) {}
