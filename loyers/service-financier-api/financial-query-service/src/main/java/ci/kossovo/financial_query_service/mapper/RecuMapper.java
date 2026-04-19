package ci.kossovo.financial_query_service.mapper;

import ci.kossovo.financial_query_service.dtos.RecuPaiementDTO;
import ci.kossovo.financial_query_service.outils.convertisseurs.MontantEnLettresService;
import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;
import ci.kossovo.financial_query_service.projection.interne.model.LocataireViewDocument;
import ci.kossovo.financial_query_service.projection.model.RecuPaiementDocument;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

// On injecte Spring pour pouvoir utiliser notre service MontantEnLettresService
@Mapper(
    componentModel = "spring",
    uses = {MontantEnLettresService.class})
public interface RecuMapper {

  @Mapping(target = "numeroRecu", source = "recuDoc.paiementId", qualifiedByName = "formatId")
  @Mapping(target = "datePaiement", source = "recuDoc.datePaiement")
  @Mapping(target = "montantPaye", source = "recuDoc.montantPaye")
  @Mapping(target = "contratId", source = "recuDoc.contratId")

  // Mappings basés sur les vues de référence
  @Mapping(
      target = "nomLocataire",
      source = "locataireDoc.nomComplet",
      defaultValue = "Locataire inconnu")
  @Mapping(
      target = "contactLocataire",
      source = "locataireDoc.contactInfo",
      defaultValue = "Non renseigné")
  @Mapping(
      target = "descriptionBien",
      source = "bienDoc.descriptionComplete",
      defaultValue = "Bien inconnu")

  // Mappings calculés
  @Mapping(target = "montantEnLettres", source = "recuDoc.montantPaye")
  // MapStruct utilisera MontantEnLettresService automatiquement
  @Mapping(
      target = "statutCompte",
      source = "recuDoc.soldeApresPaiement",
      qualifiedByName = "formatStatut")
  RecuPaiementDTO toDto(
      RecuPaiementDocument recuDoc,
      LocataireViewDocument locataireDoc,
      BienImmobilierViewDocument bienDoc);

  // --- Méthodes utilitaires pour MapStruct ---

  @Named("formatId")
  default String formatId(String id) {
    if (id == null) return null;
    return id.toUpperCase().substring(0, 8);
    // On garde juste le début du UUID pour faire plus propre
  }

  @Named("formatStatut")
  default String formatStatut(BigDecimal soldeApresPaiement) {
    if (soldeApresPaiement == null) return "Inconnu";

    if (soldeApresPaiement.compareTo(BigDecimal.ZERO) == 0) {
      return "Solde à jour (0 FCFA)";
    } else if (soldeApresPaiement.compareTo(BigDecimal.ZERO) < 0) {
      return "Reste à payer : " + soldeApresPaiement.abs().intValue() + " FCFA";
    } else {
      return "Avance de : " + soldeApresPaiement.intValue() + " FCFA";
    }
  }
}
