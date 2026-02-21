package ci.kossovo.financial_command_service.scheduler;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

@Component
public class LoyerGenerationScheduler {
    
  private final CommandGateway commandGateway;

  public LoyerGenerationScheduler(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }
}

