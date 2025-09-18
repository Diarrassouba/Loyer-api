package ci.kossovo.immobilier_rest_api.repositories;

import ci.kossovo.immobilier_rest_api.model.Appartement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppartementRepository extends JpaRepository<Appartement, String> {
}
