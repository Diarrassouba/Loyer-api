package ci.kossovo.tenancy_query_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI tenancyQueryOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("API de Consultation des Locations (Tenancy Query Service)")
                .description(
                    "Ce service CQRS (côté Lecture) fournit des vues matérialisées optimisées sur"
                        + " l'état des biens immobiliers, la hiérarchie Maisons/Appartements et"
                        + " leur disponibilité actuelle. Il s'appuie sur MongoDB pour des lectures"
                        + " ultra-rapides.")
                .version("v1.0.0")
                .contact(
                    new Contact()
                        .name("Équipe de Développement de kossovo")
                        .email("ddrissaci@gmail.com")
                        .url("https://votre-site-web.com"))
                .license(
                    new License().name("Licence API").url("https://votre-domaine.com/licence")));
  }
}
