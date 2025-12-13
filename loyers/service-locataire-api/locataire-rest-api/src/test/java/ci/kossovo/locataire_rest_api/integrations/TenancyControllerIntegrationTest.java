package ci.kossovo.locataire_rest_api.integrations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ci.kossovo.locataire_rest_api.dtos.ContratRequestDTO;
import ci.kossovo.locataire_rest_api.dtos.LocataireRequestDTO;
import ci.kossovo.locataire_rest_api.models.ContratLocation;
import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;
import ci.kossovo.locataire_rest_api.models.HistoriqueLocataire;
import ci.kossovo.locataire_rest_api.repositories.ContratRepository;
import ci.kossovo.locataire_rest_api.repositories.DisponibiliteBienRepository;
import ci.kossovo.locataire_rest_api.repositories.HistoriqueLocataireRepository;
import ci.kossovo.locataire_rest_api.repositories.LocataireRepository;
import ci.kossovo.loyer_core_api.events.locations.ContratCreatedEvent;
import ci.kossovo.loyer_core_api.events.locations.ContratFinishedEvent;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public class TenancyControllerIntegrationTest {

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
        private ContratRepository contratRepository;
        @Autowired
        private LocataireRepository locataireRepository;
        @Autowired
        private DisponibiliteBienRepository disponibiliteRepository;
        @Autowired
        private HistoriqueLocataireRepository historiqueRepository;

        @MockitoBean
        private EventGateway eventGateway;

        @AfterEach
        void tearDown() {
                contratRepository.deleteAll();
                locataireRepository.deleteAll();
                disponibiliteRepository.deleteAll();
                historiqueRepository.deleteAll();
        }

        @Test
        @DisplayName("POST /api/contrats - Doit créer le contrat si toutes les conditions sont remplies")
        void createContrat_shouldSucceed() throws Exception {
                // Arrange
                // 1. Préparer les données dans les projections
                disponibiliteRepository.save(new DisponibiliteBien("apt-dispo-1"));
                // Pas besoin de créer un historique pour un "bon" locataire

                // 2. Préparer la requête
                ContratRequestDTO requestDTO = new ContratRequestDTO(LocalDate.now(), new BigDecimal("1200"),
                                "loc-ok-1", "apt-dispo-1", null);

                // Act & Assert
                mockMvc.perform(post("/api/contrats").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.bienId").value("apt-dispo-1"));

                assertThat(contratRepository.count()).isEqualTo(1);
                verify(eventGateway).publish(any(ContratCreatedEvent.class));
        }

        @Test
        @DisplayName("POST /api/contrats - Échec : Doit retourner 400 et un message clair si le bien est loué")
        void createContrat_shouldReturn400_whenBienIsTaken() throws Exception {
                // Arrange
                String bienId = "apt-loue-1";
                DisponibiliteBien bienLoue = new DisponibiliteBien(bienId);
                bienLoue.setStatut(DisponibiliteBien.Statut.LOUE);
                disponibiliteRepository.save(bienLoue);
                ContratRequestDTO requestDTO = new ContratRequestDTO(LocalDate.now(), new BigDecimal("1200"), "loc-1",
                                bienId, null);

                // Act & Assert
                mockMvc.perform(post("/api/contrats").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status", is(400)))
                                .andExpect(jsonPath("$.error", is("Bad Request"))).andExpect(jsonPath("$.message",
                                                is("Le bien immobilier " + bienId + " est déjà loué."))); // <--
                                                                                                          // Vérification
                                                                                                          // enrichie

                assertThat(contratRepository.count()).isZero();
                verify(eventGateway, never()).publish(any(ContratCreatedEvent.class)); // On vérifie qu'aucun événement
                                                                                       // n'est
                                                                                       // publié
        }

        @Test
        @DisplayName("POST /api/contrats - Échec : Doit retourner 400 et un message clair si le locataire a un mauvais score")
        void createContrat_shouldReturn400_whenLocataireHasBadScore() throws Exception {
                // Arrange
                String locataireId = "loc-mauvais-score";
                disponibiliteRepository.save(new DisponibiliteBien("apt-1"));

                HistoriqueLocataire mauvaisLocataire = new HistoriqueLocataire(locataireId);
                mauvaisLocataire.setNombreDeNotations(1);
                mauvaisLocataire.setScoreMoyenGeneral(2.0); // Score inférieur au seuil de 2.5
                historiqueRepository.save(mauvaisLocataire);

                ContratRequestDTO requestDTO = new ContratRequestDTO(LocalDate.now(), new BigDecimal("1000"),
                                locataireId, "apt-1", null);

                // Act & Assert
                mockMvc.perform(post("/api/contrats").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message",
                                                is("Le locataire a un score de comportement insuffisant."))); // <--
                                                                                                              // Vérification
                                                                                                              // enrichie
        }

        @Test
        @DisplayName("POST /api/contrats - Échec : Doit retourner 400 pour des données de requête invalides (DTO validation)")
        void createContrat_shouldReturn400_forInvalidRequestData() throws Exception {
                // Arrange
                // Montant du loyer négatif, ce qui viole @Positive
                ContratRequestDTO requestDTO = new ContratRequestDTO(LocalDate.now(), new BigDecimal("-100"), "loc-1",
                                "apt-1", null);

                // Act & Assert
                mockMvc.perform(post("/api/contrats").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", is("La validation de la requête a échoué")))
                                .andExpect(jsonPath("$.validationErrors.montantLoyerBase").exists()); // On vérifie que
                                                                                                      // l'erreur
                                                                                                      // concerne bien
                                                                                                      // le bon champ
        }

        @Test
        @DisplayName("POST /api/contrats - Doit retourner 400 Bad Request si le bien est déjà loué")
        void createContrat_shouldFail_whenBienIsTaken() throws Exception {
                // Arrange
                // 1. Préparer la projection pour simuler un bien LOUÉ
                DisponibiliteBien bienLoue = new DisponibiliteBien("apt-loue-1");
                bienLoue.setStatut(DisponibiliteBien.Statut.LOUE);
                disponibiliteRepository.save(bienLoue);

                // 2. Préparer la requête
                ContratRequestDTO requestDTO = new ContratRequestDTO(LocalDate.now(), new BigDecimal("1200"),
                                "loc-ok-1", "apt-loue-1", null);

                // Act & Assert
                mockMvc.perform(post("/api/contrats").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest()); // Le
                                                                     // GlobalExceptionHandler
                                                                     // transforme
                                                                     // IllegalStateException
                                                                     // en 400

                assertThat(contratRepository.count()).isZero();
        }

        @Test
        @DisplayName("POST /api/locataires - Doit créer un nouveau locataire")
        void createLocataire_shouldSucceed() throws Exception {
                // Arrange
                LocataireRequestDTO locataireRequest = new LocataireRequestDTO("Kone", "Moussa",
                                "kone.moussa@email.com", "0123456789");
                // Act & Assert
                mockMvc.perform(post("/api/locataires").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(locataireRequest)))
                                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.nom").value("Kone"))
                                .andExpect(jsonPath("$.prenom").value("Moussa"))
                                .andExpect(jsonPath("$.email").value("kone.moussa@email.com"))
                                .andExpect(jsonPath("$.telephone").value("0123456789"));
                assertThat(locataireRepository.count()).isEqualTo(1);
        }

        // --- Tests pour la terminaison de contrat ---

        @Test
        @DisplayName("POST /api/contrats/{id}/terminer - Succès : Doit terminer un contrat actif et retourner 200 OK")
        void terminerContrat_shouldSucceed_forActiveContrat() throws Exception {
                // Arrange
                ContratLocation contratActif = new ContratLocation();
                contratActif.setActif(true);
                ContratLocation savedContrat = contratRepository.save(contratActif);

                // Act & Assert
                mockMvc.perform(post("/api/contrats/{id}/terminer", savedContrat.getId())).andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(savedContrat.getId())))
                                .andExpect(jsonPath("$.actif", is(false)));

                verify(eventGateway).publish(any(ContratFinishedEvent.class));
                ContratLocation updatedContrat = contratRepository.findById(savedContrat.getId()).get();
                assertThat(updatedContrat.isActif()).isFalse();
        }

        @Test
        @DisplayName("POST /api/contrats/{id}/terminer - Échec : Doit retourner 404 Not Found si le contrat n'existe pas")
        void terminerContrat_shouldReturn404_whenContratNotFound() throws Exception {
                mockMvc.perform(post("/api/contrats/{id}/terminer", "contrat-inexistant"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", is("Contrat non trouvé: contrat-inexistant")));

                verify(eventGateway, never()).publish(any(ContratFinishedEvent.class));
        }

}
