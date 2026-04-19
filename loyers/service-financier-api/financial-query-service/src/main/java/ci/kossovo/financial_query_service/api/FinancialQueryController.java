package ci.kossovo.financial_query_service.api;

import ci.kossovo.financial_query_service.dtos.RecetteMensuelleDTO;
import ci.kossovo.financial_query_service.dtos.RecuPaiementDTO;
import ci.kossovo.financial_query_service.outils.convertisseurs.ExcelGeneratorService;
import ci.kossovo.financial_query_service.projection.model.TransactionDocument;
import ci.kossovo.financial_query_service.service.FinancialQueryService;
import ci.kossovo.financial_query_service.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query/finance")
@Tag(name = "Consultations Financières", description = "Lecture des données financières (MongoDB)")
public class FinancialQueryController {

  private final FinancialQueryService queryService;
  private final PdfGeneratorService pdfGeneratorService;
  private final ExcelGeneratorService excelGeneratorService;

  public FinancialQueryController(
      FinancialQueryService queryService,
      PdfGeneratorService pdfGeneratorService,
      ExcelGeneratorService excelGeneratorService) {
    this.queryService = queryService;
    this.pdfGeneratorService = pdfGeneratorService;
    this.excelGeneratorService = excelGeneratorService;
  }

  // ... endpoints GET pour synthèses et historique ...

  @Operation(
      summary = "Imprimer/Générer un reçu de paiement",
      description = "Retourne les détails formatés pour l'impression d'un reçu")
  @GetMapping("/recus/{paiementId}")
  public ResponseEntity<RecuPaiementDTO> imprimerRecu(@PathVariable String paiementId) {
    RecuPaiementDTO recu = queryService.genererRecu(paiementId);
    return ResponseEntity.ok(recu);
  }

  @Operation(summary = "Télécharge la quittance de loyer au format PDF")
  @GetMapping("/recus/{paiementId}/pdf")
  public ResponseEntity<byte[]> telechargerRecuPdf(@PathVariable String paiementId) {

    // 1. Récupérer les données formatées (JSON)
    RecuPaiementDTO recuDTO = queryService.genererRecu(paiementId);

    // 2. Générer le fichier PDF en mémoire
    byte[] pdfBytes = pdfGeneratorService.genererRecuPdf(recuDTO);

    // 3. Préparer les Headers HTTP pour indiquer qu'il s'agit d'un fichier à télécharger
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);

    // "attachment" force le téléchargement. "inline" essaie de l'ouvrir dans le navigateur.
    String nomFichier = "quittance_" + paiementId.substring(0, 8) + ".pdf";
    headers.setContentDispositionFormData("attachment", nomFichier);

    // Optionnel : Empêcher la mise en cache
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    // 4. Renvoyer le fichier
    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
  }

  // NOUVEAU ENDPOINT (Version 4 colonnes)
  @Operation(
      summary = "Télécharge la quittance au format PDF (Mise en page compacte sur 4 colonnes)")
  @GetMapping("/recus/{paiementId}/pdf/compact")
  public ResponseEntity<byte[]> telechargerRecuPdfCompact(@PathVariable String paiementId) {

    // 1. Récupérer les données (le DTO reste le même !)
    RecuPaiementDTO recuDTO = queryService.genererRecu(paiementId);

    // 2. Générer le PDF avec la NOUVELLE méthode (4 colonnes)
    byte[] pdfBytes = pdfGeneratorService.genererRecuPdf4Colonnes(recuDTO);

    // 3. Retourner la réponse
    return construireReponsePdf(pdfBytes, paiementId);
  }

  // --- Méthode utilitaire pour factoriser la création des Headers HTTP ---
  private ResponseEntity<byte[]> construireReponsePdf(byte[] pdfBytes, String paiementId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);

    // Formatage propre du nom de fichier (ex: quittance_1234ABCD.pdf)
    String idCourt = paiementId.length() > 8 ? paiementId.substring(0, 8) : paiementId;
    String nomFichier = "quittance_" + idCourt + ".pdf";

    headers.setContentDispositionFormData("attachment", nomFichier);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
  }

  @Operation(summary = "Exporte l'historique complet d'un contrat en Excel (.xlsx)")
  @GetMapping("/contrats/{contratId}/export/excel")
  public ResponseEntity<byte[]> exporterHistoriqueExcel(@PathVariable String contratId) {

    // 1. Appel au SERVICE (et non plus au Repository) pour récupérer les données
    List<TransactionDocument> transactions =
        queryService.getTransactionsBrutesPourContrat(contratId);

    if (transactions.isEmpty()) {
      return ResponseEntity.noContent().build(); // 204 No Content si aucune transaction
    }

    // 2. Générer le fichier Excel
    byte[] excelBytes = excelGeneratorService.genererHistoriqueExcel(transactions);

    // 3. Préparer les headers et retourner la réponse
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData(
        "attachment", "historique_financier_" + contratId + ".xlsx");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
  }

  @Operation(summary = "Tableau de bord : Récupère le total des recettes encaissées par mois")
  @GetMapping("/statistiques/recettes-mensuelles")
  public ResponseEntity<List<RecetteMensuelleDTO>> getRecettesMensuelles() {
    List<RecetteMensuelleDTO> stats = queryService.getStatistiquesRecettesMensuelles();
    return ResponseEntity.ok(stats);
  }
}
