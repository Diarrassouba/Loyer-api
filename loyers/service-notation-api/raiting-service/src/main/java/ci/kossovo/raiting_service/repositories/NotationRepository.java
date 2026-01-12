package ci.kossovo.raiting_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ci.kossovo.raiting_service.models.Notation;

@Repository
public interface NotationRepository extends JpaRepository<Notation, String> {
  List<Notation> findByLocataireId(String locataireId);
}
