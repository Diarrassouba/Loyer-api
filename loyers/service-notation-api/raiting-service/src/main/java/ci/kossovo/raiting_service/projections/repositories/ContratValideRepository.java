package ci.kossovo.raiting_service.projections.repositories;

import ci.kossovo.raiting_service.projections.models.ContratValideView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratValideRepository extends JpaRepository<ContratValideView, String> {}
