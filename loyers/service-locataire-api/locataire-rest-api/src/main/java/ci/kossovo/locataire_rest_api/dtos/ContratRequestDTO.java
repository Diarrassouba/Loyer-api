package ci.kossovo.locataire_rest_api.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratRequestDTO(
    @NotNull(message = "La date de début est obligatoire")
        @FutureOrPresent(message = "La date de début ne peut pas être dans le passé")
        LocalDate dateDebut,
    @NotNull(message = "Le montant du loyer est obligatoire")
        @Positive(message = "Le montant du loyer doit être strictement positif")
        BigDecimal montantLoyerBase,
    @NotBlank(message = "L'identifiant du locataire est obligatoire") String locataireId,

    // --- Choix du bien à louer ---
    // Le client doit fournir SOIT l'appartementId, SOIT le maisonId.
    // La validation ("un et un seul") est gérée par la logique métier du Service
    // car les annotations standards ne gèrent pas facilement les conditions croisées.
    String appartementId,
    String maisonId,
    @NotNull @Positive(message = "L'avance de loyer est obligatoire")
        BigDecimal montantAvance // Le client saisit l'avance (ex: 1, 2 ou 3 mois d'avance)
    ) {}
