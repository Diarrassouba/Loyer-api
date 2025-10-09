package ci.kossovo.immobilier_rest_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonResponseDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseResponseDTO;
import ci.kossovo.immobilier_rest_api.mappers.ImmobilierMapper;
import ci.kossovo.immobilier_rest_api.model.Depense;
import ci.kossovo.immobilier_rest_api.model.Maison;
import ci.kossovo.immobilier_rest_api.repositories.AppartementRepository;
import ci.kossovo.immobilier_rest_api.repositories.DepenseRepository;
import ci.kossovo.immobilier_rest_api.repositories.MaisonRepository;
import ci.kossovo.immobilier_rest_api.services.impl.MaisonServiceImpl;
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;
import ci.kossovo.loyer_core_api.events.immobiliers.DepenseRecordedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonCreatedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonDeletedEvent;
import ci.kossovo.loyer_core_api.events.immobiliers.MaisonUpdatedEvent;
import ci.kossovo.loyer_core_api.exceptions.MaisonNotFoundException;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MaisonServiceImplTest {
  @Mock
  private MaisonRepository maisonRepository;
  @Mock
  private ImmobilierMapper mapper;
  @Mock
  private AppartementRepository appartementRepository;
  @Mock
  private DepenseRepository depenseRepository;
  @Mock
  private EventGateway eventGateway;
  @InjectMocks
  private MaisonServiceImpl maisonService;

  // Ajoutez ici vos méthodes de test

  @Test
  void createMaison_shouldSaveAndPublishEvent_whenGivenValidDTO() {
    // Implémentez votre test ici
    // 1. Créez un DTO valide
    // 2. Simulez le comportement des dépendances
    // 3. Appelez la méthode à tester
    // 4. Vérifiez que les interactions avec les dépendances sont correctes

    // ARRANGE (Préparation)
    MaisonRequestDTO requestDTO = new MaisonRequestDTO("123 ilot de Test", "123 ilot de Test", "Abidjan", 2023);

    Maison maisonToSave = new Maison();
    maisonToSave.setLot("123 ilot de Test");
    maisonToSave.setVille("123 ilot de Test");
    maisonToSave.setAnneeConstruction(2023);
    Maison savedMaison = new Maison(); // L'objet que le repo est censé retourner
    savedMaison.setId("maison-uuid-123");
    savedMaison.setLot("123 ilot de Test");
    savedMaison.setQuartier("Yopougon");
    MaisonResponseDTO expectedResponse = new MaisonResponseDTO("maison-uuid-123", "123 ilot de Test", "Yopougon",
        "Abidjan", 2023, null);

    // Définir le comportement des mocks
    when(mapper.toMaison(requestDTO)).thenReturn(maisonToSave);
    when(maisonRepository.save(maisonToSave)).thenReturn(savedMaison);
    when(mapper.toMaisonResponseDTO(savedMaison)).thenReturn(expectedResponse);

    // ACT (Action)
    MaisonResponseDTO actualResponse = maisonService.createMaison(requestDTO);

    // ASSERT (Vérification)
    assertThat(actualResponse).isNotNull();
    assertThat(actualResponse.id()).isEqualTo("maison-uuid-123");
    assertThat(actualResponse.lot()).isEqualTo("123 ilot de Test");

    // Vérifier que les méthodes des mocks ont été appelées
    verify(maisonRepository, times(1)).save(maisonToSave);
    verify(eventGateway, times(1)).publish(any(MaisonCreatedEvent.class));
  }

  @Test
  void findMaisonById_shouldThrowException_whenMaisonNotFound() {
    // ARRANGE
    String nonExistentId = "id-qui-n-existe-pas";
    when(maisonRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.findMaisonById(nonExistentId)).isInstanceOf(MaisonNotFoundException.class)
        .hasMessageContaining("Maison non trouvée avec l'ID: " + nonExistentId);

    // Vérifier qu'aucune autre interaction n'a eu lieu
    verifyNoInteractions(mapper, eventGateway);
  }

  @Test
  void updateMaison_shouldUpdateAndPublishEvent_whenMaisonExists() {
    // ARRANGE
    String maisonId = "maison-existant-id";
    MaisonRequestDTO requestDTO = new MaisonRequestDTO("Nouveau lot", "Yopougon", "Nouvelleville", 2024);

    Maison maisonExistante = new Maison();
    maisonExistante.setId(maisonId);
    maisonExistante.setLot("Nouveau lot");

    Maison maisonMiseAJour = new Maison();
    maisonMiseAJour.setId(maisonId);
    maisonMiseAJour.setLot("Nouveau lot");

    MaisonResponseDTO expectedResponse = new MaisonResponseDTO(maisonId, "Nouveau lot", "Yopougon", "Nouvelleville",
        2024, null);

    // Définir le comportement des mocks
    when(maisonRepository.findById(maisonId)).thenReturn(Optional.of(maisonExistante));
    when(maisonRepository.save(any(Maison.class))).thenReturn(maisonMiseAJour); // On peut être plus précis si besoin
    when(mapper.toMaisonResponseDTO(maisonMiseAJour)).thenReturn(expectedResponse);

    // ACT
    MaisonResponseDTO actualResponse = maisonService.updateMaison(maisonId, requestDTO);

    // ASSERT
    assertThat(actualResponse).isEqualTo(expectedResponse);

    // Vérifier que la méthode de mise à jour du mapper a été appelée sur la bonne
    // entité
    verify(mapper, times(1)).updateMaisonFromDto(requestDTO, maisonExistante);
    verify(maisonRepository, times(1)).save(maisonExistante);

    // Vérifier que l'événement de mise à jour a été publié
    verify(eventGateway, times(1)).publish(any(MaisonUpdatedEvent.class));
  }

  @Test
  void updateMaison_shouldThrowException_whenMaisonNotFound() {
    // ARRANGE
    String nonExistentId = "id-qui-n-existe-pas";
    MaisonRequestDTO requestDTO = new MaisonRequestDTO("Nouveau lot", "Yopougon", "Nouvelleville", 2024);
    when(maisonRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.updateMaison(nonExistentId, requestDTO))
        .isInstanceOf(MaisonNotFoundException.class);

    // Vérifier qu'aucune autre interaction n'a eu lieu
    verify(maisonRepository, never()).save(any());
    verifyNoInteractions(eventGateway);
  }

  @Test
  void deleteMaison_shouldDeleteAndPublishEvent_whenMaisonExists() {
    // ARRANGE
    String maisonId = "maison-a-supprimer-id";
    when(maisonRepository.existsById(maisonId)).thenReturn(true);

    // Créer un "ArgumentCaptor" pour capturer l'événement qui sera publié
    ArgumentCaptor<MaisonDeletedEvent> eventCaptor = ArgumentCaptor.forClass(MaisonDeletedEvent.class);

    // ACT
    maisonService.deleteMaison(maisonId);

    // ASSERT
    // Vérifier que la méthode delete a été appelée avec le bon ID
    verify(maisonRepository, times(1)).deleteById(maisonId);

    // Vérifier que l'événement a été publié et capturer sa valeur
    verify(eventGateway, times(1)).publish(eventCaptor.capture());

    // Vérifier que l'ID de la maison dans l'événement capturé est correct
    MaisonDeletedEvent publishedEvent = eventCaptor.getValue();
    assertThat(publishedEvent.maisonId()).isEqualTo(maisonId);
  }

  @Test
  void deleteMaison_shouldThrowException_whenMaisonNotFound() {
    // ARRANGE
    String nonExistentId = "id-qui-n-existe-pas";
    when(maisonRepository.existsById(nonExistentId)).thenReturn(false);

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.deleteMaison(nonExistentId)).isInstanceOf(MaisonNotFoundException.class);

    // Vérifier qu'aucune suppression ou publication n'a eu lieu
    verify(maisonRepository, never()).deleteById(any());
    verifyNoInteractions(eventGateway);
  }

  // TESTS POUR LA GESTION DES DÉPENSES ---

  @Test
  @DisplayName("createDepense - Doit créer la dépense et publier un événement pour une Maison")
  void createDepense_shouldSucceed_forMaison() {
    // ARRANGE
    String maisonId = "maison-id-123";
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(new BigDecimal("500"), "Réparation toiture", LocalDate.now(),
        TypeDepense.REPARATION, maisonId, null);

    Depense depenseToSave = new Depense();
    Depense savedDepense = new Depense();
    savedDepense.setId("depense-id-789");
    savedDepense.setMaisonId(maisonId);
    savedDepense.setMontant(new BigDecimal("500"));

    when(maisonRepository.existsById(maisonId)).thenReturn(true);
    when(mapper.toDepense(requestDTO)).thenReturn(depenseToSave);
    when(depenseRepository.save(depenseToSave)).thenReturn(savedDepense);

    ArgumentCaptor<DepenseRecordedEvent> eventCaptor = ArgumentCaptor.forClass(DepenseRecordedEvent.class);

    // ACT
    maisonService.createDepense(requestDTO);

    // ASSERT
    verify(depenseRepository, times(1)).save(depenseToSave);
    verify(eventGateway, times(1)).publish(eventCaptor.capture());

    DepenseRecordedEvent publishedEvent = eventCaptor.getValue();
    assertThat(publishedEvent.bienImmobilierId()).isEqualTo(maisonId);
    assertThat(publishedEvent.typeBien()).isEqualTo("MAISON");
    assertThat(publishedEvent.montant()).isEqualByComparingTo("500");
  }

  @Test
  @DisplayName("createDepense - Doit créer la dépense et publier un événement pour un Appartement")
  void createDepense_shouldSucceed_forAppartement() {
    // ARRANGE
    String appartementId = "apt-id-456";
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(new BigDecimal("120"), "Changement robinet", LocalDate.now(),
        TypeDepense.REPARATION, null, appartementId);

    when(appartementRepository.existsById(appartementId)).thenReturn(true);
    // ... Mocks pour mapper et save ...
    when(mapper.toDepense(requestDTO)).thenReturn(new Depense());
    when(depenseRepository.save(any(Depense.class))).thenReturn(new Depense());

    // ACT
    maisonService.createDepense(requestDTO);

    // ASSERT
    verify(depenseRepository).save(any(Depense.class));
    verify(eventGateway).publish(any(DepenseRecordedEvent.class));
  }

  @Test
  @DisplayName("createDepense - Doit lever IllegalArgumentException si liée aux deux biens")
  void createDepense_shouldFail_whenLinkedToBoth() {
    // ARRANGE
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(BigDecimal.ONE, "Test", LocalDate.now(), TypeDepense.DIVERS,
        "maison-id", "apt-id");

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.createDepense(requestDTO)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Une dépense ne peut pas être associée à la fois à une maison et un appartement.");

    verifyNoInteractions(depenseRepository, eventGateway);
  }

  @Test
  @DisplayName("createDepense - Doit lever IllegalArgumentException si liée à aucun bien")
  void createDepense_shouldFail_whenLinkedToNone() {
    // ARRANGE
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(BigDecimal.ONE, "Test", LocalDate.now(), TypeDepense.DIVERS,
        null, null);

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.createDepense(requestDTO)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Une dépense doit être associée à une maison ou un appartement.");
  }

  @Test
  @DisplayName("createDepense - Doit lever MaisonNotFoundException si la maison n'existe pas")
  void createDepense_shouldFail_whenMaisonNotFound() {
    // ARRANGE
    String nonExistentMaisonId = "maison-inexistante-id";
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(BigDecimal.TEN, "Test", LocalDate.now(), TypeDepense.DIVERS,
        nonExistentMaisonId, null);
    when(maisonRepository.existsById(nonExistentMaisonId)).thenReturn(false);

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.createDepense(requestDTO)).isInstanceOf(MaisonNotFoundException.class)
        .hasMessage("Maison non trouvée avec l'ID: " + nonExistentMaisonId);
  }

  @Test
  @DisplayName("createDepense - Doit lever MaisonNotFoundException si l'appartement n'existe pas")
  void createDepense_shouldFail_whenAppartementNotFound() {
    // ARRANGE
    String nonExistentAppartementId = "apt-inexistant-id";
    DepenseRequestDTO requestDTO = new DepenseRequestDTO(BigDecimal.TEN, "Test", LocalDate.now(), TypeDepense.DIVERS,
        null, nonExistentAppartementId);
    when(appartementRepository.existsById(nonExistentAppartementId)).thenReturn(false);

    // ACT & ASSERT
    assertThatThrownBy(() -> maisonService.createDepense(requestDTO)).isInstanceOf(MaisonNotFoundException.class)
        .hasMessage("Appartement non trouvé avec l'ID: " + nonExistentAppartementId);
  }


   @Test
    @DisplayName("findDepensesByMaisonId - Doit retourner la liste des dépenses")
    void findDepensesByMaisonId_shouldReturnDepenseList() {
        // ARRANGE
        String maisonId = "maison-id-123";
        Depense depense = new Depense(); // Simuler une dépense
        List<Depense> depenses = Collections.singletonList(depense);

        when(maisonRepository.existsById(maisonId)).thenReturn(true);
        when(depenseRepository.findByMaisonId(maisonId)).thenReturn(depenses);

        // ACT
        List<DepenseResponseDTO> result = maisonService.findDepensesByMaisonId(maisonId);

        // ASSERT
        verify(mapper, times(1)).toDepenseResponseDTOList(depenses);
        // On pourrait aussi vérifier le contenu de la liste si le mapper était configuré
        assertThat(result).isNotNull();
    }

  // @Test
  // void createDepense_shouldThrowException_whenMaisonNotFound() {
  // // ARRANGE
  // String maisonId = "maison-inexistante-id";
  // DepenseRequestDTO requestDTO = new DepenseRequestDTO(
  // 150.0, "Remplacement du robinet de la cuisine", "2024-01-18", "REPARATION",
  // maisonId,null);
  // when(maisonRepository.findById(maisonId)).thenReturn(Optional.empty());

  // // ACT & ASSERT
  // assertThatThrownBy(() ->
  // maisonService.createDepense(requestDTO)).isInstanceOf(MaisonNotFoundException.class)
  // .hasMessageContaining("Maison non trouvée avec l'ID: " + maisonId);

  // // Vérifier qu'aucune autre interaction n'a eu lieu
  // verifyNoInteractions(mapper, eventGateway, appartementRepository);
  // }

}
