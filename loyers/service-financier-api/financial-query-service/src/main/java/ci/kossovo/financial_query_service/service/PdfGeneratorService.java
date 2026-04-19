package ci.kossovo.financial_query_service.service;

import ci.kossovo.financial_query_service.dtos.RecuPaiementDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

  public byte[] genererRecuPdf(RecuPaiementDTO recu) {
    // Flux en mémoire pour stocker le PDF généré
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      // Initialisation d'iText
      PdfWriter writer = new PdfWriter(outputStream);
      PdfDocument pdf = new PdfDocument(writer);
      Document document = new Document(pdf);

      // --- EN-TÊTE ---
      Paragraph titre =
          new Paragraph("QUITTANCE DE LOYER / REÇU DE PAIEMENT")
              .setBold()
              .setFontSize(18)
              .setTextAlignment(TextAlignment.CENTER)
              .setMarginBottom(20);
      document.add(titre);

      // --- INFO ENTREPRISE & DATE ---
      Table headerTable =
          new Table(UnitValue.createPercentArray(new float[] {1, 1})).useAllAvailableWidth();

      Cell entrepriseCell =
          new Cell()
              .add(
                  new Paragraph(
                          "AGENCE IMMOBILIÈRE KOSSOVO\nYopougon Wassakara\n Abidjan, Cote d'Ivoire")
                      .setBold())
              .setBorder(Border.NO_BORDER);

      String dateFormattee = recu.datePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      Cell dateCell =
          new Cell()
              .add(
                  new Paragraph(
                      "Date : "
                          + dateFormattee
                          + "\nReçu N° : "
                          + recu.numeroRecu().substring(0, 8)))
              .setTextAlignment(TextAlignment.RIGHT)
              .setBorder(Border.NO_BORDER);

      headerTable.addCell(entrepriseCell);
      headerTable.addCell(dateCell);
      document.add(headerTable).add(new Paragraph("\n"));

      // --- DÉTAILS DU PAIEMENT (Tableau) ---
      // On peut passer à 4 colonnes pour une meilleure mise en page si nécessaire,
      // ou garder 2 colonnes et multiplier les lignes. Restons sur 2 colonnes.
      Table tableDetails =
          new Table(UnitValue.createPercentArray(new float[] {1, 2})).useAllAvailableWidth();
      tableDetails.setMarginBottom(20);

      // Lignes du tableau
      ajouterLigneTableau(tableDetails, "Locataire :", recu.nomLocataire(), true);
      ajouterLigneTableau(tableDetails, "Contact :", recu.contactLocataire(), false); // NOUVEAU
      ajouterLigneTableau(tableDetails, "Bien loué :", recu.descriptionBien(), false);

      // Ligne vide pour aérer
      tableDetails.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER).setMinHeight(10f));

      ajouterLigneTableau(tableDetails, "Contrat N° :", recu.contratId(), true);
      ajouterLigneTableau(tableDetails, "Montant payé :", recu.montantPaye() + " FCFA", false);

      // Mise en évidence du statut du compte
      Cell labelStatut =
          new Cell()
              .add(new Paragraph("Statut du compte :").setBold())
              .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
              .setPadding(5);
      Cell valeurStatut =
          new Cell()
              .add(new Paragraph(recu.statutCompte()).setBold())
              .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
              .setPadding(5);

      // Optionnel : Changer la couleur selon le statut (Vert = OK, Rouge = Retard)
      if (recu.statutCompte().contains("Reste à payer")) {
        valeurStatut.setFontColor(ColorConstants.RED);
      } else if (recu.statutCompte().contains("Solde à jour")) {
        valeurStatut.setFontColor(ColorConstants.DARK_GRAY); // Vert foncé
      }
      tableDetails.addCell(labelStatut);
      tableDetails.addCell(valeurStatut);

      document.add(tableDetails);
      // --- MONTANT EN LETTRES ---
      Paragraph montantLettres =
          new Paragraph("Arrêté la présente quittance à la somme de :").setItalic();
      Paragraph somme =
          new Paragraph(recu.montantEnLettres())
              .setBold()
              .setFontSize(12)
              .setBackgroundColor(ColorConstants.LIGHT_GRAY)
              .setPadding(5);

      document.add(montantLettres);
      document.add(somme).add(new Paragraph("\n\n"));

      // --- SIGNATURE ---
      Paragraph signature =
          new Paragraph("La Direction / Le Propriétaire\n(Signature et Cachet)")
              .setTextAlignment(TextAlignment.RIGHT)
              .setMarginTop(30);
      document.add(signature);

      // Fermeture du document
      document.close();

    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la génération du PDF", e);
    }

    return outputStream.toByteArray();
  }

  // ===================================================================
  // NOUVELLE MÉTHODE : MISE EN PAGE SUR 4 COLONNES
  // ===================================================================
  public byte[] genererRecuPdf4Colonnes(RecuPaiementDTO recu) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      PdfWriter writer = new PdfWriter(outputStream);
      PdfDocument pdf = new PdfDocument(writer);
      Document document = new Document(pdf);

      // --- EN-TÊTE COMMUN ---
      Paragraph titre =
          new Paragraph("QUITTANCE DE LOYER / REÇU DE PAIEMENT")
              .setBold()
              .setFontSize(18)
              .setTextAlignment(TextAlignment.CENTER)
              .setMarginBottom(20);
      document.add(titre);

      // --- INFO ENTREPRISE & DATE ---
      Table headerTable =
          new Table(UnitValue.createPercentArray(new float[] {1, 1})).useAllAvailableWidth();
      Cell entrepriseCell =
          new Cell()
              .add(
                  new Paragraph("AGENCE IMMOBILIÈRE XYZ\n123 Rue de la Paix\nParis, France")
                      .setBold())
              .setBorder(Border.NO_BORDER);

      String dateFormattee = recu.datePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      Cell dateCell =
          new Cell()
              .add(new Paragraph("Date : " + dateFormattee + "\nReçu N° : " + recu.numeroRecu()))
              .setTextAlignment(TextAlignment.RIGHT)
              .setBorder(Border.NO_BORDER);

      headerTable.addCell(entrepriseCell);
      headerTable.addCell(dateCell);
      document.add(headerTable).add(new Paragraph("\n"));

      // --- DÉTAILS DU PAIEMENT (Tableau à 4 colonnes) ---
      // On crée un tableau avec 4 colonnes de largeurs relatives (ex: 15%, 35%, 15%, 35%)
      Table tableDetails =
          new Table(UnitValue.createPercentArray(new float[] {1.5f, 3.5f, 1.5f, 3.5f}))
              .useAllAvailableWidth();
      tableDetails.setMarginBottom(20);

      // Ligne 1 : Informations sur le Locataire et le Contrat
      ajouterCelluleTableau(tableDetails, "Locataire :", true);
      ajouterCelluleTableau(tableDetails, recu.nomLocataire(), false);
      ajouterCelluleTableau(tableDetails, "Contrat N° :", true);
      ajouterCelluleTableau(tableDetails, recu.contratId(), false);

      // Ligne 2 : Informations de Contact et Description du Bien
      ajouterCelluleTableau(tableDetails, "Contact :", true);
      ajouterCelluleTableau(tableDetails, recu.contactLocataire(), false);
      ajouterCelluleTableau(tableDetails, "Bien loué :", true);
      ajouterCelluleTableau(tableDetails, recu.descriptionBien(), false);

      // Ligne vide pour aérer (fusionne les 4 colonnes)
      Cell emptyRow = new Cell(1, 4).setBorder(Border.NO_BORDER).setMinHeight(10f);
      tableDetails.addCell(emptyRow);

      // Ligne 3 : Informations Financières
      ajouterCelluleTableau(tableDetails, "Montant payé :", true);

      // Mise en évidence du montant payé
      Cell montantCell =
          new Cell()
              .add(new Paragraph(recu.montantPaye() + " FCFA").setBold())
              .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
              .setPadding(5);
      tableDetails.addCell(montantCell);

      ajouterCelluleTableau(tableDetails, "Statut compte :", true);

      // Mise en évidence du statut du compte avec couleur
      Cell statutCell =
          new Cell()
              .add(new Paragraph(recu.statutCompte()).setBold())
              .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
              .setPadding(5);
      if (recu.statutCompte().contains("Reste à payer")) {
        statutCell.setFontColor(ColorConstants.RED);
      } else if (recu.statutCompte().contains("Solde à jour")) {
        statutCell.setFontColor(ColorConstants.DARK_GRAY);
      }
      tableDetails.addCell(statutCell);

      document.add(tableDetails);

      // --- MONTANT EN LETTRES ---
      Paragraph texteMontantLettres =
          new Paragraph("Arrêté la présente quittance à la somme de :").setItalic();
      Paragraph sommeEnLettres =
          new Paragraph(recu.montantEnLettres())
              .setBold()
              .setFontSize(12)
              .setBackgroundColor(ColorConstants.LIGHT_GRAY)
              .setPadding(5);

      document.add(texteMontantLettres);
      document.add(sommeEnLettres).add(new Paragraph("\n\n"));

      // --- SIGNATURE ---
      Paragraph signature =
          new Paragraph("La Direction / Le Propriétaire\n(Signature et Cachet)")
              .setTextAlignment(TextAlignment.RIGHT)
              .setMarginTop(30);
      document.add(signature);

      document.close();

    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la génération du PDF (4 colonnes)", e);
    }

    return outputStream.toByteArray();
  }

  // ===================================================================
  // MÉTHODE UTILITAIRE POUR AJOUTER UNE LIGNE AU TABLEAU

  // Méthode utilitaire pour formatter les lignes du tableau
  private void ajouterLigneTableau(Table table, String label, String valeur, boolean isHeader) {
    String safeLabel = safeString(label);
    String safeValeur = safeString(valeur);
    Cell labelCell =
        new Cell()
            .add(new Paragraph(safeLabel).setBold())
            .setBorder(new SolidBorder(1))
            .setPadding(5);
    Cell valeurCell =
        new Cell().add(new Paragraph(safeValeur)).setBorder(new SolidBorder(1)).setPadding(5);

    if (isHeader) {
      labelCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
      valeurCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }

    table.addCell(labelCell);
    table.addCell(valeurCell);
  }

  // --- Méthode utilitaire simplifiée pour gérer les cellules ---
  private void ajouterCelluleTableau(Table table, String texte, boolean isLabel) {

    String safeValeur = safeString(texte); // Assure que le texte n'est jamais null
    Cell cell =
        new Cell()
            .add(new Paragraph(safeValeur))
            .setBorder(new SolidBorder(ColorConstants.GRAY, 1))
            .setPadding(5);
    if (isLabel) {
      cell.setBold();
      cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
    table.addCell(cell);
  }

  // --- Méthode utilitaire commune ---
  private String safeString(Object value) {
    return Objects.toString(value, "");
  }
}
