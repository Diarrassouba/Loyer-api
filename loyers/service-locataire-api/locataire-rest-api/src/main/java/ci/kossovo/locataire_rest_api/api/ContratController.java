package ci.kossovo.locataire_rest_api.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.services.ContratService;
import ci.kossovo.loyer_core_api.dtos.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contrats")
@Tag(name = "Gestion des Contrats", description = "API pour la gestion des contrats de location")
public class ContratController {

    private final ContratService contratService;

    public ContratController(ContratService contratService) {
        this.contratService = contratService;
    }

    @Operation(summary = "Crée un nouveau contrat de location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contrat créé avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ContratResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données de la requête invalides", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    @PostMapping
    public ResponseEntity<ContratResponseDTO> createContrat(@Valid @RequestBody ContratRequestDTO requestDTO) {
        ContratResponseDTO createdContrat = contratService.createContrat(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContrat);
    }

    @Operation(summary = "Termine un contrat de location existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrat terminé avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ContratResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    @PostMapping("/{id}/terminer")
    public ContratResponseDTO terminerContrat(@PathVariable String id) {
        return contratService.terminerContrat(id);
    }

    @Operation(summary = "Récupère un contrat de location par son ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Contrat trouvé", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ContratResponseDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))) })

    @GetMapping("/{id}")
    public ContratResponseDTO getContratById(@PathVariable String id) {
        return contratService.findContratById(id);
    }

}
