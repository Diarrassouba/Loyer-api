package ci.kossovo.immobilier_rest_api.repositories;

import ci.kossovo.immobilier_rest_api.model.Appartement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppartementRepository extends JpaRepository<Appartement, String> {

  // Méthode personnalisée pour trouver tous les appartements d'une maison
  List<Appartement> findByMaisonId(String maisonId);
}
