package ci.kossovo.locataire_rest_api.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.ContratResponseDTO;
import ci.kossovo.locataire_rest_api.services.ContratService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contrats")
public class ContratController {

    private final ContratService contratService;

    public ContratController(ContratService contratService) {
        this.contratService = contratService;
    }


     @PostMapping
    public ResponseEntity<ContratResponseDTO> createContrat(@Valid @RequestBody ContratRequestDTO requestDTO) {
        ContratResponseDTO createdContrat = contratService.createContrat(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContrat);
    }
    
    @PostMapping("/{id}/terminer")
    public ContratResponseDTO terminerContrat(@PathVariable String id) {
        return contratService.terminerContrat(id);
    }
    
    @GetMapping("/{id}")
    public ContratResponseDTO getContratById(@PathVariable String id) {
        return contratService.findContratById(id);
    }

    
}
