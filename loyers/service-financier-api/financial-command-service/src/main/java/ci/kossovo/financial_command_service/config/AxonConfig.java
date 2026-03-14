package ci.kossovo.financial_command_service.config;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Bean
  public SnapshotTriggerDefinition monDeclencheurSnapshot(Snapshotter snapshotter) {
    // C'est ici, en pur Java, que vous définissez que c'est un compteur ("EventCount...")
    // et que le seuil est de 50.
    // CELA REMPLACE les deux lignes du fichier application.properties.
    return new EventCountSnapshotTriggerDefinition(snapshotter, 50);
  }
}
