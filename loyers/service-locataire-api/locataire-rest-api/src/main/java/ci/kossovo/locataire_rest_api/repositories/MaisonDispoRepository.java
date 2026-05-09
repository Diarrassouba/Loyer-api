package ci.kossovo.locataire_rest_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ci.kossovo.locataire_rest_api.models.MaisonDispoView;

public interface MaisonDispoRepository  extends JpaRepository<MaisonDispoView, String>{}
