package ci.kossovo.financial_command_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaiementRequestDTO(
    @NotNull @Positive BigDecimal montant, 
    @NotNull LocalDate datePaiement
) {}
