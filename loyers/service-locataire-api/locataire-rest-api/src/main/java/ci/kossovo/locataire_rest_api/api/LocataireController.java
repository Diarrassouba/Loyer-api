package ci.kossovo.locataire_rest_api.api;

import ci.kossovo.locataire_rest_api.dtos.LocataireDTO;
import ci.kossovo.locataire_rest_api.services.ContratService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locataires")
public class LocataireController {

  private final ContratService contratService;

  public LocataireController(ContratService contratService) {
    this.contratService = contratService;
  }

   @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocataireDTO createLocataire(@Valid @RequestBody LocataireDTO locataireDTO) {
        return contratService.createLocataire(locataireDTO);
    }
    
    @GetMapping
    public List<LocataireDTO> getAllLocataires() {
        return contratService.findAllLocataires();
    }

    @GetMapping("/{id}")
    public LocataireDTO getLocataireById(@PathVariable String id) {
        return contratService.findLocataireById(id);
    }

  @PutMapping("path/{id}")
  public LocataireDTO updateLocataire(
      @PathVariable String id, @RequestBody LocataireDTO locataireDTO) {

    return contratService.updateLocataire(id, locataireDTO);
  }


}
