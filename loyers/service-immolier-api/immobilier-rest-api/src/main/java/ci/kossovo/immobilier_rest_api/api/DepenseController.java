package ci.kossovo.immobilier_rest_api.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseResponseDTO;
import ci.kossovo.immobilier_rest_api.services.MaisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api") // Route de base
@Tag(name = "Gestion des Dépenses", description = "API pour les dépenses des biens immobiliers")
public class DepenseController {

    private final MaisonService maisonService;

    public DepenseController(MaisonService maisonService) {
        this.maisonService = maisonService;
    }

    @Operation(summary = "Enregistre une nouvelle dépense pour un bien immobilier")
    @PostMapping("/depenses")
    public ResponseEntity<DepenseResponseDTO> createDepense(@Valid @RequestBody DepenseRequestDTO requestDTO) {
        DepenseResponseDTO createdDepense = maisonService.createDepense(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepense);
    }

    @Operation(summary = "Mettre à jour une dépense pour un bien immobilier spécifique")
    @PutMapping("/depenses/{depenseId}")
    public ResponseEntity<DepenseResponseDTO> updateDepense(@PathVariable String depenseId,
            @Valid @RequestBody DepenseRequestDTO requestDTO) {
        DepenseResponseDTO updatedDepense = maisonService.updateDepense(depenseId, requestDTO);
        return ResponseEntity.ok(updatedDepense);
    }

    @Operation(summary = "Supprime une dépense pour un bien immobilier spécifique")
    @DeleteMapping("/depenses/{depenseId}")
    public ResponseEntity<Void> deleteDepense(@PathVariable String depenseId) {
        maisonService.deleteDepense(depenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupère toutes les dépenses pour un bien immobilier spécifique")
    @GetMapping("/maisons/{maisonId}/depenses")
    public List<DepenseResponseDTO> getDepensesByMaison(@PathVariable String maisonId) {
        return maisonService.findDepensesByMaisonId(maisonId);
    }

    @Operation(summary = "Récupère toutes les dépenses pour un appartement spécifique")
    // Note: L'URL est un peu longue, mais elle est explicite et RESTful.
    @GetMapping("/maisons/{maisonId}/appartements/{appartementId}/depenses")
    public List<DepenseResponseDTO> getDepensesByAppartement(@PathVariable String maisonId,
            @PathVariable String appartementId) {
        // Ici, on pourrait ajouter une vérification pour s'assurer que l'appartement
        // appartient bien à la maison, pour la robustesse de l'API.
        return maisonService.findDepensesByAppartementId(appartementId);
    }

}
