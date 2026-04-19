package ci.kossovo.financial_query_service.service;

import ci.kossovo.financial_query_service.dtos.RecetteMensuelleDTO;
import ci.kossovo.financial_query_service.dtos.RecuPaiementDTO;
import ci.kossovo.financial_query_service.mapper.RecuMapper;
import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;
import ci.kossovo.financial_query_service.projection.interne.model.LocataireViewDocument;
import ci.kossovo.financial_query_service.projection.interne.repository.BienImmobilierViewRepository;
import ci.kossovo.financial_query_service.projection.interne.repository.LocataireViewRepository;
import ci.kossovo.financial_query_service.projection.model.RecuPaiementDocument;
import ci.kossovo.financial_query_service.projection.model.SyntheseFinanciereDocument;
import ci.kossovo.financial_query_service.projection.model.TransactionDocument;
import ci.kossovo.financial_query_service.repository.RecuPaiementRepository;
import ci.kossovo.financial_query_service.repository.SyntheseRepository;
import ci.kossovo.financial_query_service.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FinancialQueryService {

  private final RecuPaiementRepository recuRepo;
  private final SyntheseRepository syntheseRepo;
  private final LocataireViewRepository locataireRepo;
  private final BienImmobilierViewRepository bienRepo;
  private final TransactionRepository transactionRepo;
  private final RecuMapper recuMapper;

  public FinancialQueryService(
      RecuPaiementRepository recuRepo,
      SyntheseRepository syntheseRepo,
      LocataireViewRepository locataireRepo,
      BienImmobilierViewRepository bienRepo,
      TransactionRepository transactionRepo,
      RecuMapper recuMapper) {
    this.recuRepo = recuRepo;
    this.syntheseRepo = syntheseRepo;
    this.locataireRepo = locataireRepo;
    this.bienRepo = bienRepo;
    this.transactionRepo = transactionRepo;
    this.recuMapper = recuMapper;
  }

  // ... méthodes pour getSynthese et getHistorique ...

  public RecuPaiementDTO genererRecu(String paiementId) {
    // 1. Récupération du reçu de base
    RecuPaiementDocument recuDoc =
        recuRepo
            .findById(paiementId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Reçu introuvable pour le paiement: " + paiementId));

    // 2. Récupération de la synthèse pour trouver le bienId
    String bienId =
        syntheseRepo
            .findById(recuDoc.getContratId())
            .map(SyntheseFinanciereDocument::getBienId)
            .orElse(null);

    // 3. Récupération des données de référence (avec null possible)
    LocataireViewDocument locataireDoc =
        locataireRepo.findById(recuDoc.getLocataireId()).orElse(null);
    BienImmobilierViewDocument bienDoc =
        bienId != null ? bienRepo.findById(bienId).orElse(null) : null;

    // 4. Assemblage via MapStruct
    return recuMapper.toDto(recuDoc, locataireDoc, bienDoc);
  }

  /**
   * NOUVELLE MÉTHODE : Récupère l'historique brut des transactions pour un contrat. Cette méthode
   * est utilisée par l'export Excel.
   */
  public List<TransactionDocument> getTransactionsBrutesPourContrat(String contratId) {
    // Optionnel : Vous pouvez ajouter une vérification pour vous assurer que le contrat existe.
    if (!syntheseRepo.existsById(contratId)) {
      throw new EntityNotFoundException("Contrat non trouvé : " + contratId);
    }

    return transactionRepo.findByContratIdOrderByDateDesc(contratId);
  }

  /**
   * Calcule le total des paiements encaissés par mois pour toute l'agence. Idéal pour un graphique
   * en barres.
   */
  public List<RecetteMensuelleDTO> getStatistiquesRecettesMensuelles() {

    // 1. Récupérer toutes les transactions depuis MongoDB
    List<TransactionDocument> toutesTransactions = transactionRepo.findAll();

    // 2. Filtrer, Grouper et Additionner avec les Streams Java
    Map<YearMonth, BigDecimal> recettesParMois =
        toutesTransactions.stream()
            // On ne garde que les entrées d'argent réelles
            .filter(tx -> "PAIEMENT".equals(tx.getType()))
            // On groupe par Mois/Année
            .collect(
                Collectors.groupingBy(
                    tx -> YearMonth.from(tx.getDate()), // Clé du dictionnaire : Le mois
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        TransactionDocument::getMontant,
                        BigDecimal::add // Valeur : La somme des montants
                        )));

    // 3. Convertir le résultat (Map) en une Liste de DTOs triée chronologiquement
    return recettesParMois.entrySet().stream()
        .sorted(Map.Entry.comparingByKey()) // Trier du plus ancien au plus récent
        .map(
            entry ->
                new RecetteMensuelleDTO(
                    entry.getKey().toString(), // Ex: "2023-10"
                    entry.getValue() // Ex: 1500000
                    ))
        .collect(Collectors.toList());
  }
}
