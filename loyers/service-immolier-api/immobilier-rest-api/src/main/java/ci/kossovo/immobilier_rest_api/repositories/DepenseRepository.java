package ci.kossovo.immobilier_rest_api.repositories;

import ci.kossovo.immobilier_rest_api.model.Depense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, String> {
  // Méthodes pour trouver les dépenses par bien
  List<Depense> findByMaisonId(String maisonId);

  List<Depense> findByAppartementId(String appartementId);
}
