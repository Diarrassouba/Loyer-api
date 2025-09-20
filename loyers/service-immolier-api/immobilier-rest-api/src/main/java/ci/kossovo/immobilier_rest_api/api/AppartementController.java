package ci.kossovo.immobilier_rest_api.api;
import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    return ResponseEntity.ok(maisonService.findAppartementsByMaisonId(maisonId));
  }

  /*  @GetMapping
     public List<AppartementResponseDTO> getAppartementsByMaison(@PathVariable String maisonId) {
         return maisonService.findAppartementsByMaisonId(maisonId);
     }
  */
  /*  @PutMapping("/{id}")
  public ResponseEntity<AppartementResponseDTO> updateAppartement(
      @PathVariable String maisonId,
      @PathVariable String id,
      @Valid @RequestBody AppartementRequestDTO requestDTO) {
    return ResponseEntity.ok(maisonService.addApparte);
  } */

  /* @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAppartement(
      @PathVariable String maisonId, @PathVariable String id) {
    maisonService.deleteAppartement(maisonId, id);
    return ResponseEntity.noContent().build();
  } */
}
