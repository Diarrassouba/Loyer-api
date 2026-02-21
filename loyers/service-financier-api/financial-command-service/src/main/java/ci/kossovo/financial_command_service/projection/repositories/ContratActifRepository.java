package ci.kossovo.financial_command_service.projection.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ci.kossovo.financial_command_service.projection.models.ContratActifView;

@Repository
public interface ContratActifRepository extends JpaRepository<ContratActifView, String> {
}