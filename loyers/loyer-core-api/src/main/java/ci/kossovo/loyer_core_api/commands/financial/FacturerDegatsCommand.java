package ci.kossovo.loyer_core_api.commands.financial;

import java.math.BigDecimal;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record FacturerDegatsCommand(
    @TargetAggregateIdentifier String contratId, BigDecimal montant, String description) {}
