package ci.kossovo.immobilier_rest_api.api;

import ci.kossovo.immobilier_rest_api.dtos.ErrorMaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maisons")
@Tag(name = "Gestion des Maisons", description = "API pour le catalogue des maisons")
public class MaisonController {

  private final MaisonService maisonService;

  public MaisonController(MaisonService maisonService) {
    this.maisonService = maisonService;
  }

  // --- Opérations sur les Maisons ---

  @Operation(summary = "Crée une nouvelle maison")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Maison créée avec succès",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MaisonResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Données de la requête invalides",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @PostMapping
  public ResponseEntity<MaisonResponseDTO> createMaison(
      @Valid @RequestBody MaisonRequestDTO requestDTO) {
    MaisonResponseDTO createdMaison = maisonService.createMaison(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMaison);
  }

  @Operation(summary = "Récupère la liste de toutes les maisons")
  @ApiResponse(
      responseCode = "200",
      description = "Liste des maisons",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = List.class)))
  @GetMapping
  public ResponseEntity<List<MaisonResponseDTO>> getAllMaisons() {
    return ResponseEntity.ok(maisonService.findAllMaisons());
  }

  @Operation(summary = "Récupère une maison par son ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Maison trouvée",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = MaisonResponseDTO.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Maison non trouvée",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @GetMapping("/{id}")
  public ResponseEntity<MaisonResponseDTO> getMaisonById(@PathVariable String id) {

    return ResponseEntity.ok(maisonService.findMaisonById(id));
  }

  @Operation(summary = "Met à jour une maison existante")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Maison mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de la requête invalides"),
        @ApiResponse(responseCode = "404", description = "Maison non trouvée")
      })
  // Pour des réponses multiples, on peut omettre le @Content pour la simplicité,
  // mais il est préférable de le spécifier comme ci-dessus.
  @PutMapping("/{id}")
  public ResponseEntity<MaisonResponseDTO> updateMaison(
      @PathVariable String id, @Valid @RequestBody MaisonRequestDTO requestDTO) {

    return ResponseEntity.ok(maisonService.updateMaison(id, requestDTO));
  }

  @Operation(summary = "Supprime une maison par son ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Maison supprimée avec succès"),
        @ApiResponse(
            responseCode = "404",
            description = "Maison non trouvée",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMaison(@PathVariable String id) {
    maisonService.deleteMaison(id);
    return ResponseEntity.noContent().build();
  }
}
