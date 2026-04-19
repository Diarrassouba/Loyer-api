package ci.kossovo.financial_query_service.outils.convertisseurs;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import ci.kossovo.financial_query_service.projection.model.TransactionDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelGeneratorService {

    public byte[] genererHistoriqueExcel(List<TransactionDocument> transactions) {
        // XSSFWorkbook est le format pour les fichiers .xlsx modernes
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Historique Financier");

            // --- 1. Création du style pour l'en-tête ---
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // --- 2. Création de la ligne d'en-tête ---
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Type", "Description", "Montant (FCFA)", "Solde après (FCFA)", "ID Contrat"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- 3. Remplissage des données ---
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;

            for (TransactionDocument tx : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(tx.getDate().format(dateFormatter));
                row.createCell(1).setCellValue(tx.getType()); // "LOYER" ou "PAIEMENT"
                row.createCell(2).setCellValue(tx.getDescription());
                row.createCell(3).setCellValue(tx.getMontant().doubleValue());
                row.createCell(4).setCellValue(tx.getSoldeApresTransaction().doubleValue());
                row.createCell(5).setCellValue(tx.getContratId());
            }

            // Ajustement automatique de la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écriture dans le flux de sortie
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }
}
