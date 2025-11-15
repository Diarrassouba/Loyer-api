package ci.kossovo.raiting_service.api;

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
    double scoreMoyen =
        (notation.getScoreProprete()
                + notation.getScoreCommunication()
                + notation.getScoreRespectVoisinage()
                + notation.getScoreRespectReglement())
            / 4.0;

    // 3. Préparer et publier l'événement
    TenantNoteEvent event =
        new TenantNoteEvent(
            notationSauvegardee.getId(),
            notationSauvegardee.getLocataireId(),
            notationSauvegardee.getContratId(),
            scoreMoyen,
            notationSauvegardee.getDateNotation());

    return eventGateway
        .publish(event)
        .thenApply(
            v -> "Notation " + notationSauvegardee.getId() + " enregistrée et événement publié.");
  }
}
