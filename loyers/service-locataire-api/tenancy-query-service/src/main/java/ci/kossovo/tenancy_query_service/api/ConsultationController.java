package ci.kossovo.tenancy_query_service.api;

import ci.kossovo.loyer_core_api.dtos.ErrorResponseDTO;
import ci.kossovo.tenancy_query_service.dtos.MaisonCompleteDTO;
import ci.kossovo.tenancy_query_service.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query/tenancy") // Chemin clairement orienté "lecture"
@Tag(
    name = "Vues Matérialisées de Location",
    description = "Endpoints pour consulter l'état des biens (Loué/Libre)")
public class ConsultationController {

  private final ConsultationService service;

  public ConsultationController(ConsultationService service) {
    this.service = service;
  }

  @Operation(summary = "Détails d'une maison et statut de ses appartements")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Maison trouvée",
            content = @Content(schema = @Schema(implementation = MaisonCompleteDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Maison non trouvée (L'ID n'existe pas)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur interne du serveur",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
      })
  @GetMapping("/maisons/{maisonId}")
  public ResponseEntity<MaisonCompleteDTO> getMaisonDetails(@PathVariable String maisonId) {
    return ResponseEntity.ok(service.getMaisonCompleteById(maisonId));
  }

  @Operation(summary = "Liste toutes les maisons")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Liste des maisons récupérée avec succès")
      })
  @GetMapping("/maisons")
  public ResponseEntity<List<MaisonCompleteDTO>> getAllMaisonsDetails() {
    return ResponseEntity.ok(service.getAllMaisonsCompletes());
  }
}
