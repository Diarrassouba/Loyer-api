package ci.kossovo.immobilier_rest_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.mappers.ImmobilierMapper;
import ci.kossovo.immobilier_rest_api.model.Maison;
import ci.kossovo.immobilier_rest_api.repositories.AppartementRepository;
import ci.kossovo.immobilier_rest_api.repositories.MaisonRepository;
import ci.kossovo.immobilier_rest_api.services.impl.MaisonServiceImpl;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import jakarta.persistence.EntityNotFoundException;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MaisonServiceImplTest {
  @Mock private MaisonRepository maisonRepository;
  @Mock private ImmobilierMapper mapper;
  @Mock private AppartementRepository appartementRepository;
  @Mock private EventGateway eventGateway;
  @InjectMocks private MaisonServiceImpl maisonService;

  // Ajoutez ici vos méthodes de test

  @Test
  void createMaison_shouldSaveAndPublishEvent_whenGivenValidDTO() {
    // Implémentez votre test ici
    // 1. Créez un DTO valide
    // 2. Simulez le comportement des dépendances
    // 3. Appelez la méthode à tester
    // 4. Vérifiez que les interactions avec les dépendances sont correctes

    // ARRANGE (Préparation)
    MaisonRequestDTO requestDTO =
        new MaisonRequestDTO("123 ilot de Test", "Testville", "Abidjan", 2023);

    Maison maisonToSave = new Maison(); // L'objet que le mapper est censé créer
    Maison savedMaison = new Maison(); // L'objet que le repo est censé retourner
    savedMaison.setId("maison-uuid-123");
    savedMaison.setQuartier("123 ilot de Test");
    MaisonResponseDTO expectedResponse =
        new MaisonResponseDTO(
            "maison-uuid-123", "123 ilot de Test", "Testville", "Abidjan", 2023, null);

    // Définir le comportement des mocks
    when(mapper.toMaison(requestDTO)).thenReturn(maisonToSave);
    when(maisonRepository.save(maisonToSave)).thenReturn(savedMaison);
    when(mapper.toMaisonResponseDTO(savedMaison)).thenReturn(expectedResponse);

    // ACT (Action)
    MaisonResponseDTO actualResponse = maisonService.createMaison(requestDTO);

    // ASSERT (Vérification)
    assertThat(actualResponse).isNotNull();
    assertThat(actualResponse.id()).isEqualTo("maison-uuid-123");
    assertThat(actualResponse.quartier()).isEqualTo("123 ilot de Test");

    // Vérifier que les méthodes des mocks ont été appelées
    verify(maisonRepository, times(1)).save(maisonToSave);
    verify(eventGateway, times(1)).publish(any(MaisonCreatedEvent.class));
  }


  @Test
  void createMaison_shouldNotSaveAndPublishEvent_whenGivenInvalidDTO() {
    // Implémentez votre test ici
    // 1. Créez un DTO invalide
    // 2. Simulez le comportement des dépendances
    // 3. Appelez la méthode à tester
    // 4. Vérifiez que les interactions avec les dépendances sont correctes

    // ARRANGE (Préparation)
    MaisonRequestDTO requestDTO =
        new MaisonRequestDTO("", "Testville", "Abidjan", 2023);

    // Définir le comportement des mocks
    when(mapper.toMaison(requestDTO)).thenReturn(null);

    // ACT (Action)
    MaisonResponseDTO actualResponse = maisonService.createMaison(requestDTO);

    // ASSERT (Vérification)
    assertThat(actualResponse).isNull();

    // Vérifier que les méthodes des mocks ont été appelées
    verify(maisonRepository, times(0)).save(any(Maison.class));
    verify(eventGateway, times(0)).publish(any(MaisonCreatedEvent.class));
  }


  @Test
    void findMaisonById_shouldThrowException_whenMaisonNotFound() {
        // ARRANGE
        String nonExistentId = "id-qui-n-existe-pas";
        when(maisonRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> maisonService.findMaisonById(nonExistentId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Maison non trouvée avec l'ID: " + nonExistentId);
            
        // Vérifier qu'aucune autre interaction n'a eu lieu
        verifyNoInteractions(mapper, eventGateway);
    }

}
