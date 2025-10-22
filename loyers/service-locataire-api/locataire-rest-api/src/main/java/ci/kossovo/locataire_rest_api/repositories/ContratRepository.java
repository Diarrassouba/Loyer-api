package ci.kossovo.locataire_rest_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ci.kossovo.locataire_rest_api.models.ContratLocation;

public interface ContratRepository extends JpaRepository<ContratLocation, String> {

}
