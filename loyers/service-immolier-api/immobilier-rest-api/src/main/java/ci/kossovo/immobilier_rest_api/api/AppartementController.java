package ci.kossovo.immobilier_rest_api.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maisons/{maisonId}/appartements")
public class AppartementController {
  // Contrôleur pour gérer les appartements d'une maison spécifique
  private final MaisonService maisonService;

  public AppartementController(MaisonService maisonService) {
    this.maisonService = maisonService;
  }

  // Méthodes pour gérer les appartements (CRUD) au sein d'une maison

  @PostMapping
  public ResponseEntity<AppartementResponseDTO> createAppartement(
      @PathVariable String maisonId, @Valid @RequestBody AppartementRequestDTO requestDTO) {

    AppartementResponseDTO createdAppartement =
        maisonService.addAppartementToMaison(maisonId, requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAppartement);
  }

  @GetMapping
  public ResponseEntity<List<AppartementResponseDTO>> getAllAppartements(
      @PathVariable String maisonId) {

    return ResponseEntity.ok(maisonService.findAllAppartementsByMaisonId(maisonId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppartementResponseDTO> getAppartementById(
      @PathVariable String maisonId, @PathVariable String id) {

    return ResponseEntity.ok(maisonService.findAppartementById(maisonId, id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppartementResponseDTO> updateAppartement(
      @PathVariable String maisonId,
      @PathVariable String id,
      @Valid @RequestBody AppartementRequestDTO requestDTO) {
    return ResponseEntity.ok(maisonService.updateAppartement(maisonId, id, requestDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAppartement(
      @PathVariable String maisonId, @PathVariable String id) {
    maisonService.deleteAppartement(maisonId, id);
    return ResponseEntity.noContent().build();
  }
}
