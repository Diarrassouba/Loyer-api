package ci.kossovo.raiting_service.projections.repositories;

import ci.kossovo.raiting_service.projections.models.LocataireValideView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocataireValideRepository extends JpaRepository<LocataireValideView, String> {}
