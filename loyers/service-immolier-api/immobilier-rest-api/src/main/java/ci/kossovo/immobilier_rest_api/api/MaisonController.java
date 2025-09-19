package ci.kossovo.immobilier_rest_api.api;

import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maisons")
public class MaisonController {

  private final MaisonService maisonService;

  public MaisonController(MaisonService maisonService) {
    this.maisonService = maisonService;
  }

  // --- Opérations sur les Maisons ---
  @PostMapping
  public ResponseEntity<MaisonResponseDTO> createMaison(
      @Valid @RequestBody MaisonRequestDTO requestDTO) {
    MaisonResponseDTO createdMaison = maisonService.createMaison(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMaison);
  }

  @GetMapping
  public ResponseEntity<List<MaisonResponseDTO>> getAllMaisons() {
    return ResponseEntity.ok(maisonService.findAllMaisons());
  }

  @GetMapping("/{id}")
  public ResponseEntity<MaisonResponseDTO> getMaisonById(@PathVariable String id) {

    return ResponseEntity.ok(maisonService.findMaisonById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MaisonResponseDTO> updateMaison(
      @PathVariable String id, @Valid @RequestBody MaisonRequestDTO requestDTO) {

    return ResponseEntity.ok(maisonService.updateMaison(id, requestDTO));
  }
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMaison(@PathVariable String id) {
    maisonService.deleteMaison(id);
    return ResponseEntity.noContent().build();
  }

  
}
