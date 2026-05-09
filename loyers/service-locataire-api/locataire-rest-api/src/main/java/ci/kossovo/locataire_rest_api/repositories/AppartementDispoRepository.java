package ci.kossovo.locataire_rest_api.repositories;

import ci.kossovo.locataire_rest_api.models.AppartementDispoView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppartementDispoRepository extends JpaRepository<AppartementDispoView, String> {}
