package ci.kossovo.financial_query_service.projection;

import ci.kossovo.financial_query_service.projection.interne.repository.ContratRepository;
import ci.kossovo.financial_query_service.projection.interne.repository.LocataireRepository;
import ci.kossovo.financial_query_service.projection.model.RecuPaiementDocument;
import ci.kossovo.financial_query_service.projection.model.SyntheseFinanciereDocument;
import ci.kossovo.financial_query_service.projection.model.TransactionDocument;
import ci.kossovo.financial_query_service.repository.RecuPaiementRepository;
import ci.kossovo.financial_query_service.repository.SyntheseRepository;
import ci.kossovo.financial_query_service.repository.TransactionRepository;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountInitialisedEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("query-finance-projections")
public class QueryFinancialProjection {

  private final RecuPaiementRepository recuPaiementRepository;
  private final SyntheseRepository syntheseFinanciereRepository;
  // private final FactureRepository factureRepository;
  // private final ContratRepository contratRepository;
  // private final LocataireRepository locataireRepository;
  private final TransactionRepository transactionRepository;

  public QueryFinancialProjection(
      RecuPaiementRepository recuPaiementRepository,
      SyntheseRepository syntheseFinanciereRepository,
      ContratRepository contratRepository,
      LocataireRepository locataireRepository,
      TransactionRepository transactionRepository) {
    this.recuPaiementRepository = recuPaiementRepository;
    this.syntheseFinanciereRepository = syntheseFinanciereRepository;
    this.transactionRepository = transactionRepository;
  }

  @EventHandler
  public void on(FinancialAccountInitialisedEvent evt) {

    // 1. Créer la synthèse avec le solde de départ (le solde négatif de la caution)
    SyntheseFinanciereDocument synthese =
        new SyntheseFinanciereDocument(evt.contratId(), evt.locataireId(), evt.bienId());
    synthese.setSolde(evt.soldeInitial()); // Ex: -200 000 FCFA
    syntheseFinanciereRepository.save(synthese);

    // 2. Ajouter une transaction d'historique pour expliquer la caution
    TransactionDocument txCaution = new TransactionDocument();
    txCaution.setTransactionId(UUID.randomUUID().toString());
    txCaution.setContratId(evt.contratId());
    txCaution.setDate(LocalDateTime.now());
    txCaution.setDescription("Facturation Dépôt de Garantie (Caution 2 mois)");
    txCaution.setType("CAUTION");
    txCaution.setMontant(evt.montantCaution().negate()); // Négatif
    txCaution.setSoldeApresTransaction(evt.soldeInitial());
    transactionRepository.save(txCaution);
  }

  @EventHandler
  public void on(RentMonthlyGeneredEvent evt) {

    // 1. Récupérer la synthèse financière existante
    SyntheseFinanciereDocument synthese =
        syntheseFinanciereRepository.findById(evt.contratId()).orElse(null);
    if (synthese != null) {
      // 2. Mettre à jour les informations de la synthèse
      synthese.setMontantLoyerMensuel(evt.montantDu());
      synthese.getSolde().subtract(evt.montantDu());
      synthese.setDernierMoisGenere(evt.moisAnnee().toString());
    }

    // 3. Sauvegarder la synthèse mise à jour
    syntheseFinanciereRepository.save(synthese);

    // 2. Ajouter une transaction d'historique
    TransactionDocument tx = new TransactionDocument();
    tx.setTransactionId(UUID.randomUUID().toString());
    tx.setContratId(evt.contratId());
    tx.setDate(evt.moisAnnee().atDay(1).atStartOfDay());
    tx.setDescription("Loyer mensuel " + evt.moisAnnee().toString());
    tx.setType("LOYER");
    tx.setMontant(evt.montantDu().negate()); // Négatif car c'est une dette
    tx.getSoldeApresTransaction().subtract(evt.montantDu());
    transactionRepository.save(tx);
  }

  @EventHandler
  public void on(PaymentReceivedEvent evt) {

    syntheseFinanciereRepository
        .findById(evt.contratId())
        .ifPresent(
            synthese -> {
              synthese.setSolde(evt.nouveauSolde());
              syntheseFinanciereRepository.save(synthese);
            });

    // 2. Ajouter une transaction d'historique
    TransactionDocument tx = new TransactionDocument();
    tx.setTransactionId(evt.paiementId().toString());
    tx.setContratId(evt.contratId());
    tx.setDate(evt.datePaiement().atStartOfDay());
    tx.setDescription("Paiement reçu");
    tx.setType("PAIEMENT");
    tx.setMontant(evt.montantPaye()); // Positif
    tx.setSoldeApresTransaction(evt.nouveauSolde());
    transactionRepository.save(tx);

    // 3. GÉNÉRER LES DONNÉES DU REÇU
    RecuPaiementDocument recu = new RecuPaiementDocument();
    recu.setPaiementId(evt.paiementId().toString());
    recu.setContratId(evt.contratId());
    recu.setLocataireId(evt.locataireId());
    recu.setMontantPaye(evt.montantPaye());
    recu.setDatePaiement(evt.datePaiement());
    recu.setSoldeAvantPaiement(evt.soldeAvantPaiement());
    recu.setSoldeApresPaiement(evt.nouveauSolde());
    recuPaiementRepository.save(recu);
  }
}
