package ci.kossovo.loyer_core_api.commands.financial;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Commande pour terminer la gestion financière d'un contrat
public record CloseFinancialAccountCommand(@TargetAggregateIdentifier String contratId) {}
