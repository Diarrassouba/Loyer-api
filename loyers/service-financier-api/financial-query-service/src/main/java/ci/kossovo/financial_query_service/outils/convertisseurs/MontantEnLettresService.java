package ci.kossovo.financial_query_service.outils.convertisseurs;

import org.springframework.stereotype.Service;
import pl.allegro.finance.tradukisto.ValueConverters;

import java.math.BigDecimal;

@Service
public class MontantEnLettresService {

    // On instancie le convertisseur une seule fois
    private final ValueConverters converter = ValueConverters.FRENCH_INTEGER;

    /**
     * Convertit un montant BigDecimal en toutes lettres avec la devise Francs CFA.
     * Gère la majuscule initiale et les éventuels centimes.
     *
     * @param montant Le montant à convertir
     * @return Le montant formaté en lettres (ex: "Cent cinquante mille Francs CFA")
     */
    public String convertirEnFrancsCFA(BigDecimal montant) {
        if (montant == null) {
            return "";
        }

        // 1. Partie entière
        int partieEntiere = montant.intValue();
        String nombreEnLettres = converter.asWords(partieEntiere);

        // 2. Assemblage avec la devise
        StringBuilder resultat = new StringBuilder(nombreEnLettres).append(" Francs CFA");

        // 3. Gestion des centimes (rare en FCFA, mais mathématiquement sécurisé)
        BigDecimal fraction = montant.remainder(BigDecimal.ONE);
        int centimes = fraction.multiply(new BigDecimal("100")).intValue();
        
        if (centimes > 0) {
            resultat.append(" et ").append(converter.asWords(centimes)).append(" centimes");
        }

        // 4. Mettre la première lettre en majuscule
        String texteFinal = resultat.toString();
        if (!texteFinal.isEmpty()) {
            return texteFinal.substring(0, 1).toUpperCase() + texteFinal.substring(1);
        }

        return texteFinal;
    }
}