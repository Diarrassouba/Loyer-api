package ci.kossovo.locataire_rest_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ci.kossovo.locataire_rest_api.models.DisponibiliteBien;

public interface DisponibiliteBienRepository extends JpaRepository<DisponibiliteBien, String> {

}
