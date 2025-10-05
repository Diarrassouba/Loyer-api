package ci.kossovo.immobilier_rest_api.api;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.AppartementResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.ErrorMaisonResponseDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maisons/{maisonId}/appartements")
@Tag(
    name = "Gestion des Appartements",
    description = "API pour le catalogue des appartements dans une maison")
public class AppartementController {
  // Contrôleur pour gérer les appartements d'une maison spécifique
  private final MaisonService maisonService;

  public AppartementController(MaisonService maisonService) {
    this.maisonService = maisonService;
  }

  // Méthodes pour gérer les appartements (CRUD) au sein d'une maison

  @Operation(summary = "Crée une nouvelle appartement dans une maison")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Appartement créé avec succès",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppartementResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Données de la requête invalides",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @PostMapping
  public ResponseEntity<AppartementResponseDTO> createAppartement(
      @PathVariable String maisonId, @Valid @RequestBody AppartementRequestDTO requestDTO) {

    AppartementResponseDTO createdAppartement =
        maisonService.addAppartementToMaison(maisonId, requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAppartement);
  }

  @Operation(summary = "Récupère la liste de toutes les appartements d'une maison")
  @ApiResponse(
      responseCode = "200",
      description = "Liste des appartements",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = List.class)))
  @GetMapping
  public ResponseEntity<List<AppartementResponseDTO>> getAllAppartements(
      @PathVariable String maisonId) {

    return ResponseEntity.ok(maisonService.findAppartementsByMaisonId(maisonId));
  }

  @Operation(summary = "Récupère un appartement par son ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Appartement trouvé",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = AppartementResponseDTO.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Appartement non trouvé",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @GetMapping("/{id}")
  public ResponseEntity<AppartementResponseDTO> getAppartementById(
      @PathVariable String maisonId, @PathVariable String id) {

    AppartementResponseDTO appartement = maisonService.findAppartementById(maisonId, id);
    return ResponseEntity.ok(appartement);
  }

  @Operation(summary = "Met à jour un appartement existant")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Appartement mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de la requête invalides"),
        @ApiResponse(responseCode = "404", description = "Appartement non trouvé")
      })
  // Pour des réponses multiples, on peut omettre le @Content pour la simplicité,
  // mais il est préférable de le spécifier comme ci-dessus.
  @PutMapping("/{id}")
  public ResponseEntity<AppartementResponseDTO> updateAppartement(
      @PathVariable String maisonId,
      @PathVariable String id,
      @Valid @RequestBody AppartementRequestDTO requestDTO) {

    AppartementResponseDTO updatedAppartement =
        maisonService.updateAppartement(maisonId, id, requestDTO);
    return ResponseEntity.ok(updatedAppartement);
  }

  @Operation(summary = "Supprime un appartement par son ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Appartement supprimé avec succès"),
        @ApiResponse(
            responseCode = "404",
            description = "Appartement non trouvé",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMaisonResponseDTO.class)))
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAppartement(
      @PathVariable String maisonId, @PathVariable String id) {
    maisonService.deleteAppartement(maisonId, id);
    return ResponseEntity.noContent().build();
  }
}
