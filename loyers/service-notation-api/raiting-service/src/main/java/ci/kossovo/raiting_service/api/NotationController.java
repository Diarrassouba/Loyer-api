package ci.kossovo.raiting_service.api;

import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.dtos.NotationResponseDTO;
import ci.kossovo.raiting_service.services.NotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notations")
@Tag(name = "Gestion des Notations", description = "API pour noter les locataires")
public class NotationController {

  private final NotationService notationService;

  // DTO pour la requête de création
  public NotationController(NotationService notationService) {
    this.notationService = notationService;
  }

  @Operation(summary = "Crée une nouvelle notation pour un locataire sur un contrat")
  @PostMapping
  public ResponseEntity<NotationResponseDTO> createNotation(
      @Valid @RequestBody CreerNotationRequest requestDTO) {
    NotationResponseDTO createdNotation = notationService.createNotation(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdNotation);
  }

  @Operation(summary = "Récupère toutes les notations pour un locataire spécifique")
  @GetMapping(params = "locataireId") // Endpoint: /api/notations?locataireId=...
  public List<NotationResponseDTO> getNotationsByLocataire(@RequestParam String locataireId) {
    return notationService.findNotationsByLocataireId(locataireId);
  }

  @Operation(summary = "Récupère une notation par son ID unique")
  @GetMapping("/{id}")
  public NotationResponseDTO getNotationById(@PathVariable String id) {
    return notationService.findNotationById(id);
  }
}
