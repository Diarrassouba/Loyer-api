package ci.kossovo.raiting_service.api;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.models.Notation;
import ci.kossovo.raiting_service.repositories.NotationRepository;

@RestController
@RequestMapping("/api/notations")
public class NotationController {

  private final NotationRepository notationRepository;
  private final EventGateway eventGateway;

  // DTO pour la requête de création
  public NotationController(NotationRepository notationRepository, EventGateway eventGateway) {
    this.notationRepository = notationRepository;
    this.eventGateway = eventGateway;
  }

  @PostMapping
  public CompletableFuture<String> creerNotation(@RequestBody CreerNotationRequest request) {
    // Logique de validation (scores entre 1 et 5, etc.)
    // ...

    // 1. Sauvegarder la notation
    Notation notation = new Notation();
    notation.setLocataireId(request.locataireId());
    notation.setContratId(request.contratId());
    notation.setDateNotation(LocalDate.now());
    notation.setScoreProprete(request.scoreProprete());
    notation.setScoreCommunication(request.scoreCommunication());
    notation.setScoreRespectVoisinage(request.scoreRespectVoisinage());
    notation.setScoreRespectReglement(request.scoreRespectReglement());
    notation.setCommentaire(request.commentaire());
    notation.setNotePar(request.notePar());

    Notation notationSauvegardee = notationRepository.save(notation);

    // 2. Calculer le score moyen
    double scoreMoyen = (notation.getScoreProprete() + notation.getScoreCommunication()
        + notation.getScoreRespectVoisinage() + notation.getScoreRespectReglement()) / 4.0;

    // 3. Préparer et publier l'événement
    TenantNoteEvent event = new TenantNoteEvent(notationSauvegardee.getId(), notationSauvegardee.getLocataireId(),
        notationSauvegardee.getContratId(), scoreMoyen, notationSauvegardee.getDateNotation());

    eventGateway.publish(event);
    return CompletableFuture
        .completedFuture("Notation " + notationSauvegardee.getId() + " enregistrée et événement publié.");
  }
}
