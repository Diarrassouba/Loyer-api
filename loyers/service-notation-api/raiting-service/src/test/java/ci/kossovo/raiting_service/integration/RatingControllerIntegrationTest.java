package ci.kossovo.raiting_service.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ci.kossovo.loyer_core_api.events.raiting.TenantNoteEvent;
import ci.kossovo.raiting_service.dtos.CreerNotationRequest;
import ci.kossovo.raiting_service.projections.models.ContratValideView;
import ci.kossovo.raiting_service.projections.models.LocataireValideView;
import ci.kossovo.raiting_service.projections.repositories.ContratValideRepository;
import ci.kossovo.raiting_service.projections.repositories.LocataireValideRepository;
import ci.kossovo.raiting_service.repositories.NotationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public class RatingControllerIntegrationTest {

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
    private NotationRepository notationRepository;
    @Autowired
    private ContratValideRepository contratValideRepository;
    @Autowired
    private LocataireValideRepository locataireValideRepository;

    @MockitoBean
    private EventGateway eventGateway;

    @AfterEach
    void tearDown() {
        notationRepository.deleteAll();
        contratValideRepository.deleteAll();
        locataireValideRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/notations - Succès : Doit créer une notation et retourner 201 Created")
    void createNotation_shouldSucceed_whenDataIsValid() throws Exception {
        // Arrange
        // 1. Préparer les données dans les projections pour que la validation réussisse
        locataireValideRepository.save(new LocataireValideView("loc-1"));
        contratValideRepository.save(new ContratValideView("contrat-1", "loc-1"));

        // 2. Préparer la requête
        CreerNotationRequest request = new CreerNotationRequest("loc-1", "contrat-1", 4, 5, 5, 4, "Très bon locataire",
                "mgr-1");

        // Act & Assert
        mockMvc.perform(post("/api/notations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty()).andExpect(jsonPath("$.locataireId", is("loc-1")))
                .andExpect(jsonPath("$.scoreMoyen", is(4.5))); // (4+5+5+4)/4 = 4.5

        assertThat(notationRepository.count()).isEqualTo(1);
        verify(eventGateway).publish(any(TenantNoteEvent.class));
    }

    @Test
    @DisplayName("POST /api/notations - Échec : Doit retourner 404 Not Found si le contrat n'existe pas")
    void createNotation_shouldReturn404_whenContratNotFound() throws Exception {
        // Arrange
        locataireValideRepository.save(new LocataireValideView("loc-1")); // Le locataire existe...
        // ...mais le contrat n'existe pas dans la projection.
        CreerNotationRequest request = new CreerNotationRequest("loc-1", "contrat-inconnu", 4, 5, 5, 4, "", "mgr-1");

        // Act & Assert
        mockMvc.perform(post("/api/notations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Contrat non trouvé avec l'ID: contrat-inconnu")));
    }

    @Test
    @DisplayName("POST /api/notations - Échec : Doit retourner 400 Bad Request si le contrat ne correspond pas au locataire")
    void createNotation_shouldReturn400_whenContratDoesNotMatchLocataire() throws Exception {
        // Arrange
        locataireValideRepository.save(new LocataireValideView("loc-1-correct"));
        // Le contrat existe, mais il est lié à un autre locataire
        contratValideRepository.save(new ContratValideView("contrat-1", "loc-2-autre"));

        CreerNotationRequest request = new CreerNotationRequest("loc-1-correct", "contrat-1", 4, 5, 5, 4, "", "mgr-1");

        // Act & Assert
        mockMvc.perform(post("/api/notations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(
                        jsonPath("$.message", is("Le contrat contrat-1 n'est pas associé au locataire loc-1-correct")));
    }

    @Test
    @DisplayName("POST /api/notations - Échec : Doit retourner 400 Bad Request pour un score invalide")
    void createNotation_shouldReturn400_forInvalidScore() throws Exception {
        // Arrange
        // Pas besoin de préparer les projections, la validation DTO échoue avant.
        CreerNotationRequest request = new CreerNotationRequest("loc-1", "contrat-1", 6, 5, 5, 4, "", "mgr-1"); // scoreProprete
                                                                                                                // > 5

        // Act & Assert
        mockMvc.perform(post("/api/notations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.scoreProprete").exists());
    }

}
