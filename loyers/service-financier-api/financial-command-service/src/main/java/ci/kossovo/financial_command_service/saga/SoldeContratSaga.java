package ci.kossovo.financial_command_service.saga;

import ci.kossovo.loyer_core_api.events.financial.LatePaymentCriticalEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReceivedEvent;
import ci.kossovo.loyer_core_api.events.financial.PaymentReminderRequiredEvent;
import ci.kossovo.loyer_core_api.events.financial.RentMonthlyGeneredEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class SoldeContratSaga {

  // --- Dépendances ---
  @Autowired private transient DeadlineManager deadlineManager;
  @Autowired private transient EventGateway eventGateway;

  // --- État interne de la Saga (persisté par Axon) ---
  private String contratId;
  private String locataireId;
  private BigDecimal soldeCourant;
  private LocalDate dateDerniereDette; // Date du premier loyer non soldé
  private String deadlineId;

  // 1. DÉMARRAGE DE LA SAGA (une seule fois par contrat)
  @StartSaga
  @SagaEventHandler(associationProperty = "contratId")
  public void on(ContratCreatedEvent evt) {
    System.out.println(
        "Saga [Contrat " + evt.contratId() + "]: Démarrage de la surveillance du solde.");
    this.contratId = evt.contratId();
    this.locataireId = evt.locataireId();
    this.soldeCourant = BigDecimal.ZERO;
    // On planifie la première vérification
    planifierProchaineVerification();
  }

  // 2. RÉACTION AUX ÉVÉNEMENTS FINANCIERS
  @SagaEventHandler(associationProperty = "contratId")
  public void on(RentMonthlyGeneredEvent evt) {
    System.out.println("Saga [Contrat " + evt.contratId() + "]: Loyer généré. Solde mis à jour.");
    this.soldeCourant = this.soldeCourant.subtract(evt.montantDu());

    // Si c'est la première fois que le solde devient négatif, on note la date.
    if (this.soldeCourant.compareTo(BigDecimal.ZERO) < 0 && dateDerniereDette == null) {
      this.dateDerniereDette = LocalDate.now();
    }
  }

  @SagaEventHandler(associationProperty = "contratId")
  public void on(PaymentReceivedEvent evt) {
    System.out.println("Saga [Contrat " + evt.contratId() + "]: Paiement reçu. Solde mis à jour.");
    // Le nouveau solde vient directement de l'événement, c'est la source de vérité.
    this.soldeCourant = evt.nouveauSolde();

    // Si le paiement a soldé la dette, on réinitialise le compteur de retard.
    if (this.soldeCourant.compareTo(BigDecimal.ZERO) >= 0) {
      this.dateDerniereDette = null;
    }
  }

  // 3. GESTION DE LA VÉRIFICATION PÉRIODIQUE (DEADLINE)

  @DeadlineHandler(deadlineName = "verificationSolde")
  public void onVerificationSolde(String contratId) {
    System.out.println(
        "Saga [Contrat "
            + contratId
            + "]: Vérification périodique du solde : "
            + this.soldeCourant);

    // Si le solde est négatif (dette)
    if (this.soldeCourant.compareTo(BigDecimal.ZERO) < 0) {
      long joursDeRetard =
          Duration.between(dateDerniereDette.atStartOfDay(), LocalDate.now().atStartOfDay())
              .toDays();

      // Règle métier : si le retard dépasse 30 jours -> alerte critique
      if (joursDeRetard > 30) {
        System.err.println(
            "Saga [Contrat " + contratId + "]: RETARD CRITIQUE de " + joursDeRetard + " jours.");
        eventGateway.publish(
            new LatePaymentCriticalEvent(
                contratId, this.locataireId, this.soldeCourant, (int) joursDeRetard));
      }
      // Règle métier : si le retard est de plus de 5 jours -> simple rappel
      else if (joursDeRetard > 10) {
        System.out.println("Saga [Contrat " + contratId + "]: Rappel de paiement requis.");
        eventGateway.publish(
            new PaymentReminderRequiredEvent(contratId, this.locataireId, this.soldeCourant));
      }
    }

    // On replanifie la prochaine vérification pour que la saga continue de tourner.
    planifierProchaineVerification();
  }

  // 4. FIN DE LA SAGA
  @EndSaga
  @SagaEventHandler(associationProperty = "contratId")
  public void on(ContratFinishedEvent evt) {
    System.out.println(
        "Saga [Contrat " + evt.contratId() + "]: Contrat terminé. Fin de la surveillance.");
    // Annuler toute deadline en attente pour éviter des exécutions fantômes.
    if (this.deadlineId != null) {
      deadlineManager.cancelSchedule("verificationSolde", this.deadlineId);
    }
  }

  // --- Méthode utilitaire ---
  private void planifierProchaineVerification() {
    // On planifie une vérification dans 7 jours, en passant le contratId comme payload.
    this.deadlineId =
        deadlineManager.schedule(Duration.ofDays(7), "verificationSolde", this.contratId);
    System.out.println(
        "Saga: Prochaine vérification du solde planifiée dans 7 jours. ID: " + this.deadlineId);
  }
}
