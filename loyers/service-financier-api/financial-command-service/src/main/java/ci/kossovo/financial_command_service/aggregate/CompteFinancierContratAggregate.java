package ci.kossovo.financial_command_service.aggregate;

import ci.kossovo.loyer_core_api.commands.financial.CloseFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.GenerateMonthlyRentCommand;
import ci.kossovo.loyer_core_api.commands.financial.InitializeFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.RecordPaymentCommand;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountCloturedEvent;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountInitialisedEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentInAdvanceDetectEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class CompteFinancierContratAggregate {

  @AggregateIdentifier private String contratId;

  private String locataireId;
  private BigDecimal montantLoyerMensuelDeBase;
  private BigDecimal soldeCourant; // Négatif = dette du locataire, Positif = avance du locataire
  private Map<YearMonth, BigDecimal> loyersDus;
  private boolean actif;

  // Constructeur par défaut requis par Axon
  public CompteFinancierContratAggregate() {}

  // 1. Gestionnaire de la commande d'initialisation
  @CommandHandler
  public CompteFinancierContratAggregate(InitializeFinancialAccountCommand cmd) {
    // Logique de validation
    if (cmd.montantLoyerMensuel().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Le montant du loyer doit être positif.");
    }

    // Publication de l'événement
    AggregateLifecycle.apply(
        new FinancialAccountInitialisedEvent(
            cmd.contratId(), cmd.locataireId(), cmd.montantLoyerMensuel()));
  }

  // 2. Gestionnaire de l'événement d'initialisation
  @EventSourcingHandler
  public void on(FinancialAccountInitialisedEvent evt) {
    this.contratId = evt.contratId();
    this.locataireId = evt.locataireId();
    this.montantLoyerMensuelDeBase = evt.montantLoyerMensuel();
    this.soldeCourant = BigDecimal.ZERO;
    this.loyersDus = Map.of(); // Initialisation d'une map vide
    this.actif = true;
  }

  // 3. Gestionnaire de la commande de génération de loyer
  @CommandHandler
  public void handle(GenerateMonthlyRentCommand cmd) {
    if (!this.actif) {
      throw new IllegalStateException("Le compte financier de ce contrat est clôturé.");
    }
    // Logique de validation : ne pas générer deux fois pour le même mois
    if (loyersDus.containsKey(cmd.moisAnnee())) {
      throw new IllegalStateException("Le loyer pour " + cmd.moisAnnee() + " a déjà été généré.");
    }

    AggregateLifecycle.apply(
        new RentMonthlyGeneredEvent(
            cmd.contratId(),
            UUID.randomUUID(), // Génère un ID unique pour ce loyer
            cmd.moisAnnee(),
            cmd.montant()));
  }

  // 4. Gestionnaire de l'événement de génération de loyer
  @EventSourcingHandler
  public void on(RentMonthlyGeneredEvent evt) {
    this.loyersDus.put(evt.moisAnnee(), evt.montantDu());

    // La dette du locataire augmente du montant du loyer généré
    this.soldeCourant = this.soldeCourant.subtract(evt.montantDu());
  }

  // 5. Gestionnaire de la commande de paiement
  @CommandHandler
  public void handle(RecordPaymentCommand cmd) {
    if (!this.actif) {
      throw new IllegalStateException("Le compte financier de ce contrat est clôturé.");
    }
    if (cmd.montant().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Le montant du paiement doit être positif.");
    }

    // Calcul du nouveau solde
    BigDecimal nouveauSolde = this.soldeCourant.add(cmd.montant());

    AggregateLifecycle.apply(
        new PaymentReceivedEvent(
            cmd.contratId(),
            this.locataireId, // On inclut le locataireId pour la projection
            cmd.paiementId(),
            cmd.montant(),
            cmd.datePaiement(),
            this.soldeCourant,
            nouveauSolde));

    // On peut aussi publier des événements métier supplémentaires
    if (nouveauSolde.compareTo(BigDecimal.ZERO) > 0) {
      AggregateLifecycle.apply(
          new PaymentInAdvanceDetectEvent(this.contratId, this.locataireId, nouveauSolde));
    }
  }

  // 6. Gestionnaire de l'événement de paiement reçu
  @EventSourcingHandler
  public void on(PaymentReceivedEvent evt) {
    // Le solde est directement mis à jour avec la valeur calculée dans l'événement.
    // C'est une bonne pratique pour éviter de recalculer et garantir la cohérence.
    this.soldeCourant = evt.nouveauSolde();
  }

  @CommandHandler
  public void handle(CloseFinancialAccountCommand cmd) {
    if (!this.actif) {
      throw new IllegalStateException("Le compte financier de ce contrat est déjà clôturé.");
    }
    if (this.soldeCourant.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalStateException("Impossible de clôturer le compte : solde négatif.");
    }

    AggregateLifecycle.apply(new FinancialAccountCloturedEvent(this.contratId, this.locataireId));
  }

  @EventSourcingHandler
  public void on(FinancialAccountCloturedEvent evt) {
    this.actif = false;
  }
}
