package ci.kossovo.locataire_rest_api.projection;

import static org.assertj.core.api.Assertions.assertThat;

import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;
import ci.kossovo.locataire_rest_api.repositories.DisponibiliteBienRepository;
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeMaison;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest // Charge uniquement le contexte JPA
public class DisponibiliteProjectionTest {

  @Autowired private DisponibiliteBienRepository disponibiliteRepository;

  // L'objet à tester : notre projection. On l'instancie manuellement car on n'a pas de contexte
  // Spring complet.
  private DisponibiliteProjection projection;

  @Test
  @DisplayName("Doit gérer le cycle de vie complet de la disponibilité d'un bien")
  public void shouldHandleFullLifecycleOfBienDisponibilite() {
    // --- ARRANGE ---
    // On instancie la projection à tester en lui passant le repository injecté par @DataJpaTest
    projection = new DisponibiliteProjection(disponibiliteRepository);

    String maisonId = "maison-123";
    String contratId = "contrat-abc20";

    // Création des événements qui simulent le flux métier
    MaisonCreatedEvent maisonCreee =
        new MaisonCreatedEvent(maisonId, "Adresse", "Abidjan", contratId,TypeMaison.COUREE.toString(), 2010  );
    ContratCreatedEvent contratCree =
        new ContratCreatedEvent(
            contratId, "loc-1", maisonId, "MAISON", BigDecimal.TEN, LocalDate.now());
    ContratFinishedEvent contratTermine =
        new ContratFinishedEvent(contratId, "loc-1", maisonId, LocalDate.now());

    // --- ACT & ASSERT - Étape 1 : Création de la maison ---
    projection.on(maisonCreee);

    Optional<DisponibiliteBien> bienViewOpt1 = disponibiliteRepository.findById(maisonId);
    assertThat(bienViewOpt1).isPresent();
    DisponibiliteBien bienView1 = bienViewOpt1.get();
    assertThat(bienView1.getBienId()).isEqualTo(maisonId);
    assertThat(bienView1.getStatut()).isEqualTo(DisponibiliteBien.Statut.DISPONIBLE);
    assertThat(bienView1.getContratIdActif()).isNull();

    // --- ACT & ASSERT - Étape 2 : Création d'un contrat pour la maison ---
    projection.on(contratCree);

    Optional<DisponibiliteBien> bienViewOpt2 = disponibiliteRepository.findById(maisonId);
    assertThat(bienViewOpt2).isPresent();
    DisponibiliteBien bienView2 = bienViewOpt2.get();
    assertThat(bienView2.getStatut()).isEqualTo(DisponibiliteBien.Statut.LOUE);
    assertThat(bienView2.getContratIdActif()).isEqualTo(contratId);

    // --- ACT & ASSERT - Étape 3 : Fin du contrat ---
    projection.on(contratTermine);

    Optional<DisponibiliteBien> bienViewOpt3 = disponibiliteRepository.findById(maisonId);
    assertThat(bienViewOpt3).isPresent();
    DisponibiliteBien bienView3 = bienViewOpt3.get();
    assertThat(bienView3.getStatut()).isEqualTo(DisponibiliteBien.Statut.DISPONIBLE);
    assertThat(bienView3.getContratIdActif()).isNull();
  }
}
