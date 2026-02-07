package ci.kossovo.raiting_service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.dtos.NotationResponseDTO;
import ci.kossovo.raiting_service.mapper.RatingMapper;
import ci.kossovo.raiting_service.models.Notation;
import ci.kossovo.raiting_service.projections.models.ContratValideView;
import ci.kossovo.raiting_service.projections.repositories.ContratValideRepository;
import ci.kossovo.raiting_service.projections.repositories.LocataireValideRepository;
import ci.kossovo.raiting_service.repositories.NotationRepository;
import ci.kossovo.raiting_service.services.impl.NotationServiceImpl;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class NotationServiceImplTest {

    @Mock
    private NotationRepository notationRepository;
    @Mock
    private RatingMapper mapper;
    @Mock
    private EventGateway eventGateway;
    @Mock
    private ContratValideRepository contratValideRepository;
    @Mock
    private LocataireValideRepository locataireValideRepository;

    @InjectMocks
    private NotationServiceImpl notationService;

    // Scénario de succès
    @Test
    @DisplayName("createNotation - Doit réussir si le contrat et le locataire sont valides")
    public void createNotation_shouldSucceed_whenIdsAreValid() {
        // Arrange
        String locataireId = "loc-valide";
        String contratId = "contrat-valide";
        CreerNotationRequest request = new CreerNotationRequest(locataireId, contratId, 5, 5, 5, 5, "Excellent",
                "manager-1");

        ContratValideView contratValide = new ContratValideView(contratId, locataireId);

        NotationResponseDTO responseDTO = new NotationResponseDTO("notation-1", locataireId, contratId, LocalDate.now(),
                5, 5, 5, 5, "Excellent", "manager-1", 5.0);

        // Simuler les validations : tout est ok
        when(locataireValideRepository.existsById(locataireId)).thenReturn(true);
        when(contratValideRepository.findById(contratId)).thenReturn(Optional.of(contratValide));
        when(mapper.toNotation(request)).thenReturn(new Notation());

        when(notationRepository.save(any(Notation.class))).thenReturn(new Notation());
        when(mapper.toNotationResponseDTO(any(Notation.class))).thenReturn(responseDTO);

        // Act
        notationService.createNotation(request);

        // Assert
        verify(notationRepository).save(any(Notation.class));
        verify(eventGateway).publish(any(TenantNoteEvent.class));
    }

    // Scénarios d'échec de la validation
    @Test
    @DisplayName("createNotation - Doit échouer si le locataire n'existe pas")
    public void createNotation_shouldFail_whenLocataireNotFound() {
        // Arrange
        String locataireId = "loc-inconnu";
        CreerNotationRequest request = new CreerNotationRequest(locataireId, "contrat-1", 5, 5, 5, 5, "", "");

        when(locataireValideRepository.existsById(locataireId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> notationService.createNotation(request)).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Locataire non trouvé");

        verifyNoInteractions(notationRepository, eventGateway);
    }

    @Test
    @DisplayName("createNotation - Doit échouer si le contrat n'appartient pas au locataire")
    public void createNotation_shouldFail_whenContratDoesNotMatchLocataire() {
        // Arrange
        String locataireId = "loc-1";
        String autreLocataireId = "loc-2";
        String contratId = "contrat-1";
        CreerNotationRequest request = new CreerNotationRequest(locataireId, contratId, 5, 5, 5, 5, "", "");

        // Le contrat existe, mais il est associé à un autre locataire
        ContratValideView contratValide = new ContratValideView(contratId, autreLocataireId);

        when(locataireValideRepository.existsById(locataireId)).thenReturn(true);
        when(contratValideRepository.findById(contratId)).thenReturn(Optional.of(contratValide));

        // Act & Assert
        assertThatThrownBy(() -> notationService.createNotation(request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("n'est pas associé au locataire");
    }

}
