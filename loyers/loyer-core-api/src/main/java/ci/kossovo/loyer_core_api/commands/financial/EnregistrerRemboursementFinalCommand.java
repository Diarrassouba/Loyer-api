package ci.kossovo.loyer_core_api.commands.financial;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour enregistrer le remboursement final d'un contrat. (RecordFinalRefundCommand)
public record EnregistrerRemboursementFinalCommand(@TargetAggregateIdentifier String contratId) {}
