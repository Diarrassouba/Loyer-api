package ci.kossovo.immobilier_rest_api.repositories;

import ci.kossovo.immobilier_rest_api.model.Maison;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaisonRepository extends JpaRepository<Maison, String> {}
