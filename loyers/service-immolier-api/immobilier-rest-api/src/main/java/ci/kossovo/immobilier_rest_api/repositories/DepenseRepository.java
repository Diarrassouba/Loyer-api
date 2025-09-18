package ci.kossovo.immobilier_rest_api.repositories;

import ci.kossovo.immobilier_rest_api.model.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, String> {
}
