package ci.kossovo.raiting_service.repositories;

import ci.kossovo.raiting_service.models.Notation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotationRepository extends JpaRepository<Notation, String> {
  List<Notation> findByLocataireId(String locataireId);
}
