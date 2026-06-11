package ci.kossovo.financial_command_service.projection.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ci.kossovo.financial_command_service.projection.models.ContratActifView;


public interface ContratActifRepository extends JpaRepository<ContratActifView, String> {
}