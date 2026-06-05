package ci.kossovo.financial_command_service.aggregate;

import ci.kossovo.loyer_core_api.commands.financial.CloseFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.EnregistrerRemboursementFinalCommand;
import ci.kossovo.loyer_core_api.commands.financial.FacturerDegatsCommand;
import ci.kossovo.loyer_core_api.commands.financial.GenerateMonthlyRentCommand;
import ci.kossovo.loyer_core_api.commands.financial.InitializeFinancialAccountCommand;
import ci.kossovo.loyer_core_api.commands.financial.RecordPaymentCommand;
import ci.kossovo.loyer_core_api.commands.financial.RestituerCautionCommand;
import ci.kossovo.loyer_core_api.events.financial.CautionRestitueeEvent;
import ci.kossovo.loyer_core_api.events.financial.CompteFinancierClotureEvent;
import ci.kossovo.loyer_core_api.events.financial.DegatsFacturesEvent;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountCloturedEvent;
import ci.kossovo.loyer_core_api.events.financial.FinancialAccountInitialisedEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentInAdvanceDetectEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate(
    snapshotTriggerDefinition =
        "monDeclencheurSnapshot") // On indique à Axon d'utiliser notre déclencheur de snapshot
// personnalisé
public class CompteFinancierContratAggregate {

  @AggregateIdentifier private String contratId;

  private String locataireId;
  private BigDecimal montantLoyerMensuelDeBase;
  // Négatif = dette du locataire, Positif = avance du locataire
  private BigDecimal soldeCourant;
  // Clé : mois/année, Valeur : montant du loyer dû pour ce mois
  private Map<YearMonth, BigDecimal> loyersDus;
  private BigDecimal montantCaution; // Stocké à l'initialisation
  private boolean actif;

  // Constructeur par défaut requis par Axon
  public CompteFinancierContratAggregate() {}

  // 1. Gestionnaire de la commande d'initialisation
  @CommandHandler
  public CompteFinancierContratAggregate(InitializeFinancialAccountCommand cmd) {
    // Le solde initial est égal à la caution en négatif (le locataire commence avec cette dette)
    BigDecimal soldeInitial = cmd.montantCaution().negate();

    // Logique de validation
    if (cmd.montantLoyerMensuel().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Le montant du loyer doit être positif.");
    }

    // Publication de l'événement
    AggregateLifecycle.apply(
        new FinancialAccountInitialisedEvent(
            cmd.contratId(),
            cmd.locataireId(),
            cmd.bienId(),
            cmd.montantLoyerMensuel(),
            cmd.montantCaution(),
            cmd.montantAvance(),
            soldeInitial // Transmet le solde de départ (ex: -200 000 FCFA)
            ));
  }

  // 2. Gestionnaire de l'événement d'initialisation
  @EventSourcingHandler
  public void on(FinancialAccountInitialisedEvent evt) {
    this.contratId = evt.contratId();
    this.locataireId = evt.locataireId();
    this.montantLoyerMensuelDeBase = evt.montantLoyerMensuel();
    this.montantCaution = evt.montantCaution(); // On stocke la caution pour référence future
    this.soldeCourant = evt.soldeInitial(); // Le solde de départ de l'agrégat est négatif (dette de caution)
    this.loyersDus = new HashMap<>(); // Initialisation d'une map vide
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
    // 1. SÉCURITÉ : Si le solde est null, on l'initialise à ZERO.
    if (this.soldeCourant == null) {
      this.soldeCourant = BigDecimal.ZERO;
    }
    AggregateLifecycle.apply(
        new RentMonthlyGeneredEvent(
            cmd.contratId(),
            UUID.randomUUID(), // Génère un ID unique pour ce loyer
            this.locataireId,
            cmd.moisAnnee(),
            cmd.montantDut(),
            this.soldeCourant.subtract(
                cmd.montantDut()) // Le nouveau solde après génération du loyer
            ));
  }

  // 4. Gestionnaire de l'événement de génération de loyer
  @EventSourcingHandler
  public void on(RentMonthlyGeneredEvent evt) {
    this.loyersDus.put(evt.moisAnnee(), evt.montantDu());

    // La dette du locataire augmente du montant du loyer généré
    // this.soldeCourant = this.soldeCourant.subtract(evt.montantDu());
    this.soldeCourant = evt.nouveauSolde();
    // On prend le solde calculé dans l'événement pour garantir la cohérence
    // On prend le solde calculé dans l'événement pour garantir la cohérence
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
    // 1. SÉCURITÉ : Si le solde est null, on l'initialise à ZERO.
    if (this.soldeCourant == null) {
      this.soldeCourant = BigDecimal.ZERO;
    }
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

  // @CommandHandler
  // public void handle(CloseFinancialAccountCommand cmd) {
  //   if (!this.actif) {
  //     throw new IllegalStateException("Le compte financier de ce contrat est déjà clôturé.");
  //   }
  //   if (this.soldeCourant.compareTo(BigDecimal.ZERO) < 0) {
  //     throw new IllegalStateException("Impossible de clôturer le compte : solde négatif.");
  //   }

  //   AggregateLifecycle.apply(new FinancialAccountCloturedEvent(this.contratId, this.locataireId));
  // }

  // @EventSourcingHandler
  // public void on(FinancialAccountCloturedEvent evt) {
  //   this.actif = false;
  // }

    @CommandHandler
    public void handle(RestituerCautionCommand cmd) {
        // Règle : Le compte doit être actif pour restituer la caution
        Optional.of(this.actif)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalStateException("Le compte est déjà clôturé."));

        BigDecimal nouveauSolde = this.soldeCourant.add(this.montantCaution); // On recrédite la caution
        
        AggregateLifecycle.apply(new CautionRestitueeEvent(this.contratId, this.montantCaution, nouveauSolde));
    }

    @EventSourcingHandler
    public void on(CautionRestitueeEvent evt) {
        this.soldeCourant = evt.nouveauSolde();
    }


     @CommandHandler
    public void handle(FacturerDegatsCommand cmd) {
        Optional.of(this.actif)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalStateException("Le compte est déjà clôturé."));

        BigDecimal nouveauSolde = this.soldeCourant.subtract(cmd.montant()); // On débite les dégâts
        
        AggregateLifecycle.apply(new DegatsFacturesEvent(this.contratId, cmd.montant(), cmd.description(), nouveauSolde));
    }

     @EventSourcingHandler
    public void on(DegatsFacturesEvent evt) {
        this.soldeCourant = evt.nouveauSolde();
    }




     @CommandHandler
    public void handle(EnregistrerRemboursementFinalCommand cmd) {
        // On ne peut clôturer que si le solde est positif (on rend l'argent) ou nul
        Optional.of(this.soldeCourant)
                .filter(solde -> solde.compareTo(BigDecimal.ZERO) >= 0)
                .orElseThrow(() -> new IllegalStateException("Impossible de clôturer : le locataire a encore des dettes (" + this.soldeCourant + " FCFA)."));

        BigDecimal montantRembourse = this.soldeCourant; // On lui rend tout le crédit restant
        BigDecimal nouveauSolde = BigDecimal.ZERO; // Le solde revient à 0

        AggregateLifecycle.apply(new CompteFinancierClotureEvent(this.contratId, montantRembourse, nouveauSolde));
    }

     @EventSourcingHandler
    public void on(CompteFinancierClotureEvent evt) {
        this.soldeCourant = evt.nouveauSolde();
        this.actif = false; // Désactivation définitive du compte financier
    }
}
