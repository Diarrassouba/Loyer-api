package ci.kossovo.financial_command_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinancialCommandServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FinancialCommandServiceApplication.class, args);
  }
}
