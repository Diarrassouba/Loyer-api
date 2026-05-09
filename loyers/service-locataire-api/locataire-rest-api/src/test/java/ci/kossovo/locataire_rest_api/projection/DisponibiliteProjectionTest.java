package ci.kossovo.locataire_rest_api.projection;

import static org.assertj.core.api.Assertions.assertThat;

import ci.kossovo.locataire_rest_api.models.AppartementDispoView;
import ci.kossovo.locataire_rest_api.models.MaisonDispoView;
import ci.kossovo.locataire_rest_api.repositories.AppartementDispoRepository;
import ci.kossovo.locataire_rest_api.repositories.MaisonDispoRepository;
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeBatiment;
import ci.kossovo.loyer_core_api.events.immobiliers.AppartementAddedToMaisonEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest // Charge uniquement le contexte JPA
public class DisponibiliteProjectionTest {

  @Autowired private MaisonDispoRepository maisonRepo;

  @Autowired private AppartementDispoRepository aptRepo;

  private DisponibiliteProjection projection;

  @BeforeEach
  void setUp() {
    // Instanciation de la projection avec les vrais repositories de test
    projection = new DisponibiliteProjection(maisonRepo, aptRepo);
  }

  @Test
  @DisplayName("Doit gérer la disponibilité lors de la location d'une MAISON ENTIÈRE")
  void shouldHandleLifecycleOfMaisonEntiere() {
    // --- ARRANGE ---
    String maisonId = "maison-villa";
    String contratId = "contrat-global";

    // --- ACT : Création ---
    projection.on(
        new MaisonCreatedEvent(
            maisonId, "Adresse", "Ville", "Quartier", TypeBatiment.VILLA.name(), 2020));

    // --- ASSERT : Création ---
    MaisonDispoView maisonInitiale = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(maisonInitiale.isLoueeEnEntier()).isFalse();

    // --- ACT : Location de la maison ---
    projection.on(
        new ContratCreatedEvent(
            contratId, "loc-1", maisonId, "MAISON", BigDecimal.TEN, LocalDate.now()));

    // --- ASSERT : Location ---
    MaisonDispoView maisonLouee = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(maisonLouee.isLoueeEnEntier()).isTrue();

    // --- ACT : Fin de contrat ---
    projection.on(
        new ContratFinishedEvent(contratId, "loc-1", maisonId, "MAISON", LocalDate.now()));

    // --- ASSERT : Fin ---
    MaisonDispoView maisonLiberee = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(maisonLiberee.isLoueeEnEntier()).isFalse();
  }

  @Test
  @DisplayName("Doit gérer la hiérarchie et les compteurs lors de la location d'un APPARTEMENT")
  void shouldHandleLifecycleOfAppartementAndUpdateParent() {
    // --- ARRANGE ---
    String maisonId = "maison-immeuble";
    String apt1Id = "apt-101";
    String apt2Id = "apt-102";
    String contratApt1 = "contrat-101";

    // Création du parent et des enfants
    projection.on(
        new MaisonCreatedEvent(
            maisonId, "Adresse", "Ville", "IMMEUBLE", TypeBatiment.IMMEUBLE.name(), 2020));
    projection.on(
        new AppartementAddedToMaisonEvent(
            contratApt1, apt1Id, maisonId, "TypeLot1", "TypeBatiment1", 3, "10 Rue de la Paix"));
    projection.on(
        new AppartementAddedToMaisonEvent(
            contratApt1, apt2Id, maisonId, "TypeLot2", "TypeBatiment2", 2, "12 Rue de la Paix"));

    // --- ASSERT : État Initial ---
    MaisonDispoView immeuble = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(immeuble.getTotalAppartements()).isEqualTo(2);
    assertThat(immeuble.getAppartementsLoues()).isZero();
    assertThat(immeuble.isLoueeEnEntier()).isFalse();

    AppartementDispoView apt1 = aptRepo.findById(apt1Id).orElseThrow();
    assertThat(apt1.isLoue()).isFalse();
    assertThat(apt1.getMaisonId()).isEqualTo(maisonId);

    // --- ACT : Location d'un seul appartement ---
    projection.on(
        new ContratCreatedEvent(
            contratApt1, "loc-1", apt1Id, "APPARTEMENT", BigDecimal.TEN, LocalDate.now()));

    // --- ASSERT : Mise à jour enfant ET parent ---
    // Vérification de l'enfant
    AppartementDispoView apt1Loue = aptRepo.findById(apt1Id).orElseThrow();
    assertThat(apt1Loue.isLoue()).isTrue();

    // Vérification du parent (le compteur doit avoir augmenté)
    MaisonDispoView immeubleApresLocation = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(immeubleApresLocation.getAppartementsLoues()).isEqualTo(1);
    assertThat(immeubleApresLocation.getTotalAppartements()).isEqualTo(2);
    assertThat(immeubleApresLocation.isLoueeEnEntier())
        .isFalse(); // L'immeuble n'est pas loué globalement

    // --- ACT : Fin du contrat de l'appartement ---
    projection.on(
        new ContratFinishedEvent(contratApt1, "loc-1", apt1Id, "APPARTEMENT", LocalDate.now()));

    // --- ASSERT : Restauration enfant ET parent ---
    AppartementDispoView apt1Libre = aptRepo.findById(apt1Id).orElseThrow();
    assertThat(apt1Libre.isLoue()).isFalse();

    MaisonDispoView immeubleApresFin = maisonRepo.findById(maisonId).orElseThrow();
    assertThat(immeubleApresFin.getAppartementsLoues()).isZero();
  }
}
