package ci.kossovo.financial_command_service.config;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.quartz.QuartzDeadlineManager;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.serialization.Serializer;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Qualifier;
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

  @Bean
  public DeadlineManager deadlineManager(
      Scheduler scheduler,
      TransactionManager transactionManager,
      @Qualifier("messageSerializer") Serializer serializer,

      // CORRECTION ICI : On utilise le chemin complet de la classe Axon directement
      org.axonframework.config.Configuration axonConfiguration) {

    return QuartzDeadlineManager.builder()
        .scheduler(scheduler)
        .transactionManager(transactionManager)
        .serializer(serializer)
        // On récupère le scopeAwareProvider
        .scopeAwareProvider(axonConfiguration.scopeAwareProvider())
        .build();
  }
}
