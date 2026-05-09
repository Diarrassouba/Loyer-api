package ci.kossovo.locataire_rest_api.api;

import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireResponseDto;
import ci.kossovo.locataire_rest_api.services.ContratService;
import ci.kossovo.loyer_core_api.dtos.ErrorResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locataires")
@Tag(name = "Gestion des Locataires", description = "API pour la gestion des locataires")
public class LocataireController {

  private final ContratService contratService;

  public LocataireController(ContratService contratService) {
    this.contratService = contratService;
  }

  @Operation(summary = "Crée un nouveau locataire")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Locataire créé avec succès",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LocataireResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Données de la requête invalides",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<LocataireResponseDto> createLocataire(
      @Valid @RequestBody LocataireRequestDTO locataireRequestDTO) {

    LocataireResponseDto locataireResponseDto = contratService.createLocataire(locataireRequestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(locataireResponseDto);
  }

  @Operation(summary = "Récupère la liste de tous les locataires")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste des locataires récupérée avec succès",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LocataireResponseDto.class))),
      })
  @GetMapping
  public List<LocataireResponseDto> getAllLocataires() {
    return contratService.findAllLocataires();
  }

  @Operation(summary = "Récupère un locataire par son ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Locataire trouvé",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LocataireResponseDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Locataire non trouvé",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
      })
  @GetMapping("/{id}")
  public LocataireResponseDto getLocataireById(@PathVariable String id) {
    return contratService.findLocataireById(id);
  }

  @Operation(summary = "Met à jour un locataire existant")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Locataire mis à jour avec succès",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LocataireResponseDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Locataire non trouvé",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class)))
      })
  @PutMapping("/{id}")
  public LocataireResponseDto updateLocataire(
      @PathVariable String id, @RequestBody LocataireRequestDTO locataireRequestDto) {

    return contratService.updateLocataire(id, locataireRequestDto);
  }
}
