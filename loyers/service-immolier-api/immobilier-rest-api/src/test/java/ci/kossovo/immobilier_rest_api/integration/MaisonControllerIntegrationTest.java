package ci.kossovo.immobilier_rest_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import ci.kossovo.immobilier_rest_api.dtos.AppartementRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.MaisonRequestDTO;
import ci.kossovo.immobilier_rest_api.dtos.depenses.DepenseRequestDTO;
import ci.kossovo.immobilier_rest_api.model.Appartement;
import ci.kossovo.immobilier_rest_api.model.Depense;
import ci.kossovo.immobilier_rest_api.model.Maison;
import ci.kossovo.immobilier_rest_api.repositories.AppartementRepository;
import ci.kossovo.immobilier_rest_api.repositories.DepenseRepository;
import ci.kossovo.immobilier_rest_api.repositories.MaisonRepository;
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeAppartement;
import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@Testcontainers // 1. Active l'intégration de Testcontainers avec JUnit 5
public class MaisonControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MaisonRepository maisonRepository;

    @Autowired
    private AppartementRepository appartementRepository;

    @Autowired
    private DepenseRepository depenseRepository;

    @MockitoBean
    private EventGateway eventGateway;

    // Nettoyer la BDD après chaque test pour garantir l'isolation
    @AfterEach
    void tearDown() {
        depenseRepository.deleteAll();
        appartementRepository.deleteAll();
        maisonRepository.deleteAll();
    }

    // --- Tests pour MaisonController ---

    @Test
    @DisplayName("POST /api/maisons - Doit créer une maison et retourner 201 Created")
    void createMaison_shouldReturn201Created() throws Exception {
        // Arrange
        MaisonRequestDTO requestDTO = new MaisonRequestDTO("214 ilot 21 Attie", "Yopougon", "Abidjan", 2000);

        // Act & Assert
        mockMvc.perform(post("/api/maisons").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty()).andExpect(jsonPath("$.lot", is("214 ilot 21 Attie")));

        assertThat(maisonRepository.findAll()).hasSize(1);
        verify(eventGateway).publish((Object) any());
    }

    @Test
    @DisplayName("GET /api/maisons/{id} - Doit retourner une maison et 200 OK si elle existe")
    void getMaisonById_shouldReturnMaisonWhenExists() throws Exception {
        // Arrange
        Maison maison = maisonRepository.save(createMaisonEntity("120 ilot 5 Selmer", "Yopougon", "Abidjan"));

        // Act & Assert
        mockMvc.perform(get("/api/maisons/{id}", maison.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(maison.getId()))).andExpect(jsonPath("$.ville", is("Abidjan")));
    }

    @Test
    @DisplayName("GET /api/maisons/{id} - Doit retourner 404 Not Found si elle n'existe pas")
    void getMaisonById_shouldReturn404WhenNotFound() throws Exception {
        // Arrange
        maisonRepository.save(createMaisonEntity("120 ilot 5 Selmer", "Yopougon", "Abidjan"));
        mockMvc.perform(get("/api/maisons/{id}", "id-inexistant")).andExpect(status().isNotFound());
    }

    // --- Tests pour AppartementController (imbriqué) ---

    @Test
    @DisplayName("POST /api/maisons/{maisonId}/appartements - Doit créer un appartement et retourner 201" + " Created")
    void createAppartement_shouldReturn201Created() throws Exception {
        // Arrange
        // 1. Il faut d'abord une maison existante pour y ajouter un appartement
        Maison maison = maisonRepository.save(createMaisonEntity("300 ilot 10 Riviera", "Cocody", "Abidjan"));
        AppartementRequestDTO aptRequest = new AppartementRequestDTO("APT 3B", TypeAppartement.VILLA, 2, 3);

        // Act & Assert
        mockMvc.perform(post("/api/maisons/{maisonId}/appartements", maison.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(aptRequest)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.reference", is("APT 3B"))).andExpect(jsonPath("$.maisonId", is(maison.getId())));

        assertThat(appartementRepository.findAll()).hasSize(1);
        verify(eventGateway).publish((Object) any());
    }

    @Test
    @DisplayName("GET /api/maisons/{maisonId}/appartements - Doit retourner la liste des appartements")
    void getAppartementsByMaison_shouldReturnList() throws Exception {
        // Arrange
        Maison maison = maisonRepository.save(createMaisonEntity("301 ilot 5 Sicogi", "Yopougon", "Abidjan"));
        // On utilise la méthode de l'API pour créer les appartements, testant ainsi les
        // deux endpoints en synergie
        createAppartementApiCall(maison.getId(), new AppartementRequestDTO("APT 02", TypeAppartement.STUDIO, 0, 0));
        createAppartementApiCall(maison.getId(), new AppartementRequestDTO("P004", TypeAppartement.VILLA, 1, 2));

        // Act & Assert
        mockMvc.perform(get("/api/maisons/{maisonId}/appartements", maison.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].reference", is("APT 02")))
                .andExpect(jsonPath("$[1].reference", is("P004")));
    }

    // --- Autres tests pour les autres endpoints (update, delete, etc.) peuvent
    // être
    // ajoutés ici ---
    @Test
    @DisplayName("PUT /api/maisons/{id} - Doit mettre à jour une maison et retourner 200 OK")
    void updateMaison_shouldReturn200Ok() throws Exception {
        // ARRANGE
        Maison maison = maisonRepository.save(createMaisonEntity("150 ilot 7 Selmer", "Yopougon", "Abidjan"));
        String maisonId = maison.getId();
        MaisonRequestDTO requestDTO = new MaisonRequestDTO("Nouveau lot", "Yopougon", "Nouvelleville", 2024);

        // ACT & ASSERT
        mockMvc.perform(put("/api/maisons/{id}", maisonId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(maisonId))).andExpect(jsonPath("$.lot", is("Nouveau lot")));

        // Vérifier que la maison a été mise à jour en base
        Maison updatedMaison = maisonRepository.findById(maisonId).orElseThrow();
        assertThat(updatedMaison.getLot()).isEqualTo("Nouveau lot");
        assertThat(updatedMaison.getVille()).isEqualTo("Nouvelleville");

        // Vérifier que l'événement de mise à jour a été publié
        verify(eventGateway).publish((Object) any());
    }

    @Test
    @DisplayName("PUT /api/maisons/{id} - Doit retourner 404 Not Found si la maison n'existe pas")
    void updateMaison_shouldReturn404WhenNotFound() throws Exception {
        // ARRANGE
        String nonExistentId = "id-qui-n-existe-pas";
        MaisonRequestDTO requestDTO = new MaisonRequestDTO("Nouveau lot", "Yopougon", "Nouvelleville", 2024);

        // ACT & ASSERT
        mockMvc.perform(put("/api/maisons/{id}", nonExistentId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isNotFound());

        // Vérifier qu'aucune maison n'a été créée
        assertThat(maisonRepository.findAll()).isEmpty();

        // Vérifier qu'aucun événement n'a été publié
        verifyNoInteractions(eventGateway);
    }

    @Test
    @DisplayName("DELETE /api/maisons/{id} - Doit supprimer une maison et retourner 204 No Content")
    void deleteMaison_shouldReturn204NoContent() throws Exception {
        // ARRANGE
        Maison maison = maisonRepository.save(createMaisonEntity("180 ilot 9 Selmer", "Yopougon", "Abidjan"));
        String maisonId = maison.getId();

        // ACT & ASSERT
        mockMvc.perform(delete("/api/maisons/{id}", maisonId)).andExpect(status().isNoContent());

        // Vérifier que la maison a été supprimée en base
        assertThat(maisonRepository.existsById(maisonId)).isFalse();

        // Vérifier que l'événement de suppression a été publié
        verify(eventGateway).publish((Object) any());
    }

    @Test
    @DisplayName("DELETE /api/maisons/{id} - Doit retourner 404 Not Found si la maison n'existe pas")
    void deleteMaison_shouldReturn404WhenNotFound() throws Exception {
        // ARRANGE
        String nonExistentId = "id-qui-n-existe-pas";

        // ACT & ASSERT
        mockMvc.perform(delete("/api/maisons/{id}", nonExistentId)).andExpect(status().isNotFound());

        // Vérifier qu'aucune maison n'a été supprimée
        assertThat(maisonRepository.findAll()).isEmpty();

        // Vérifier qu'aucun événement n'a été publié
        verifyNoInteractions(eventGateway);
    }

    @Test
    @DisplayName("POST /api/maisons/{maisonId}/appartements - Doit retourner 404 Not Found si la maison n'existe pas")
    void createAppartement_shouldReturn404WhenMaisonNotFound() throws Exception {
        // ARRANGE
        String nonExistentMaisonId = "maison-id-inexistante";
        AppartementRequestDTO aptRequest = new AppartementRequestDTO("APT 3B", TypeAppartement.VILLA, 2, 3);

        // ACT & ASSERT
        mockMvc.perform(post("/api/maisons/{maisonId}/appartements", nonExistentMaisonId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(aptRequest)))
                .andExpect(status().isNotFound());

        // Vérifier qu'aucun appartement n'a été créé
        assertThat(appartementRepository.findAll()).isEmpty();

        // Vérifier qu'aucun événement n'a été publié
        verifyNoInteractions(eventGateway);
    }

    @Test
    @DisplayName("GET /api/maisons/{maisonId}/appartements - Doit retourner 404 Not Found si la maison n'existe pas")
    void getAppartementsByMaison_shouldReturn404WhenMaisonNotFound() throws Exception {
        // ARRANGE
        String nonExistentMaisonId = "maison-id-inexistante";

        // ACT & ASSERT
        mockMvc.perform(get("/api/maisons/{maisonId}/appartements", nonExistentMaisonId))
                .andExpect(status().isNotFound());

        // Vérifier qu'aucun appartement n'a été créé
        assertThat(appartementRepository.findAll()).isEmpty();

        // Vérifier qu'aucun événement n'a été publié
        verifyNoInteractions(eventGateway);
    }

    @Test
    @DisplayName("GET /api/maisons - Doit retourner la liste des maisons")
    void getAllMaisons_shouldReturnList() throws Exception {
        // Arrange
        maisonRepository.save(createMaisonEntity("301 ilot 5 Sicogi", "Yopougon", "Abidjan"));
        maisonRepository.save(createMaisonEntity("302 ilot 6 Sicogi", "Koumassi", "Abidjan"));

        // Act & Assert
        mockMvc.perform(get("/api/maisons")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lot", is("301 ilot 5 Sicogi")))
                .andExpect(jsonPath("$[1].lot", is("302 ilot 6 Sicogi")));
    }

    @Test
    @DisplayName("GET /api/maisons - Doit retourner une liste vide si aucune maison n'existe")
    void getAllMaisons_shouldReturnEmptyListWhenNoMaisons() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/maisons")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    // --- Méthodes utilitaires pour garder les tests propres ---

    private Maison createMaisonEntity(String lot, String quartier, String ville) {
        Maison maison = new Maison();
        maison.setLot(lot);
        maison.setQuartier(quartier);
        maison.setVille(ville);
        return maison;
    }

    private void createAppartementApiCall(String maisonId, AppartementRequestDTO dto) throws Exception {
        mockMvc.perform(post("/api/maisons/{maisonId}/appartements", maisonId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    // TESTS D'INTÉGRATION POUR DepenseController ---

    @Test
    @DisplayName("POST /api/depenses - Doit créer une dépense pour une maison et retourner 201 Created")
    void createDepense_forMaison_shouldSucceed() throws Exception {
        // Arrange
        Maison maison = maisonRepository.save(createMaisonEntity("Avenue Montaigne", "Abidjan"));
        DepenseRequestDTO requestDTO = new DepenseRequestDTO(new BigDecimal("1250.99"), "Reprise de la peinture",
                LocalDate.now(), TypeDepense.REPARATION, maison.getId(), null);

        // Act & Assert
        mockMvc.perform(post("/api/depenses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.description", is("Reprise de la peinture")))
                .andExpect(jsonPath("$.maisonId", is(maison.getId()))).andExpect(jsonPath("$.appartementId").isEmpty());

        assertThat(depenseRepository.findAll()).hasSize(1);
        verify(eventGateway).publish((Object) any());
    }

    @Test
    @DisplayName("POST /api/depenses - Doit créer une dépense pour un appartement et retourner 201 Created")
    void createDepense_forAppartement_shouldSucceed() throws Exception {
        // Arrange
        Maison maison = maisonRepository.save(createMaisonEntity("Sideci", "Abidjan"));
        Appartement appartement = createAppartementEntity(maison, "3ème étage");
        appartementRepository.save(appartement);

        DepenseRequestDTO requestDTO = new DepenseRequestDTO(new BigDecimal("230000"), "Plomberie salle de bain",
                LocalDate.now(), TypeDepense.REPARATION, null, appartement.getId());

        // Act & Assert
        mockMvc.perform(post("/api/depenses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Plomberie salle de bain")))
                .andExpect(jsonPath("$.maisonId").isEmpty())
                .andExpect(jsonPath("$.appartementId", is(appartement.getId())));
    }

    @Test
    @DisplayName("POST /api/depenses - Doit retourner 400 Bad Request si liée à aucun bien")
    void createDepense_shouldReturn400_whenNoAssociation() throws Exception {
        // Arrange
        DepenseRequestDTO requestDTO = new DepenseRequestDTO(BigDecimal.TEN, "Dépense orpheline", LocalDate.now(),
                TypeDepense.DIVERS, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/depenses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isBadRequest());
    }


     @Test
    @DisplayName("POST /api/depenses - Doit retourner 404 Not Found si la maison n'existe pas")
    void createDepense_shouldReturn404_whenMaisonNotFound() throws Exception {
        // Arrange
        DepenseRequestDTO requestDTO = new DepenseRequestDTO(
            BigDecimal.TEN, "Test", LocalDate.now(), TypeDepense.DIVERS, "maison-inexistante-id", null
        );

        // Act & Assert
        mockMvc.perform(post("/api/depenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isNotFound());
    }

     @Test
    @DisplayName("GET /api/maisons/{maisonId}/depenses - Doit retourner la liste des dépenses pour une maison")
    void getDepensesByMaison_shouldReturnDepenseList() throws Exception {
        // Arrange
        Maison maison1 = maisonRepository.save(createMaisonEntity("Maison 1", "Ville A"));
        Maison maison2 = maisonRepository.save(createMaisonEntity("Maison 2", "Ville B"));

        depenseRepository.save(createDepenseEntity(maison1, null, "Dépense M1-A"));
        depenseRepository.save(createDepenseEntity(maison1, null, "Dépense M1-B"));
        depenseRepository.save(createDepenseEntity(maison2, null, "Dépense M2"));

        // Act & Assert
        mockMvc.perform(get("/api/maisons/{maisonId}/depenses", maison1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].description").value("Dépense M1-A"))
            .andExpect(jsonPath("$[1].description").value("Dépense M1-B"));
    }

    // --- Méthodes utilitaires ---

    private Maison createMaisonEntity(String quartier, String ville) {
        Maison maison = new Maison();
        maison.setQuartier(quartier);
        maison.setVille(ville);
        maison.setLot("750 ilot 12 Attié");
        return maison;
    }

    private Appartement createAppartementEntity(Maison maison, String reference) {
        Appartement apt = new Appartement();
        apt.setReference(reference);
        apt.setMaison(maison);
        return apt;
    }

    private Depense createDepenseEntity(Maison maison, Appartement apt, String description) {
        Depense depense = new Depense();
        depense.setMontant(BigDecimal.TEN);
        depense.setDescription(description);
        depense.setDate(LocalDate.now());
        depense.setTypeDepense(TypeDepense.DIVERS);
        if (maison != null)
            depense.setMaisonId(maison.getId());
        if (apt != null)
            depense.setAppartementId(apt.getId());
        return depense;
    }
}
