package ci.kossovo.financial_query_service.projection.interne.repository;

import ci.kossovo.financial_query_service.projection.interne.model.Locataire;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocataireRepository extends MongoRepository<Locataire, String> {}
