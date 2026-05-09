package ci.kossovo.locataire_rest_api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratResponseDTO(
    String id,

    // L'identifiant unifié du bien loué (que ce soit une maison ou un appt)
    String bienId,

    // Indique si le 'bienId' pointe vers une MAISON ou un APPARTEMENT
    String typeBien,
    String locataireId,
    BigDecimal montantLoyerBase,
    LocalDate dateDebut,
    LocalDate dateFin, // Sera null tant que le contrat est actif
    boolean actif) {}
