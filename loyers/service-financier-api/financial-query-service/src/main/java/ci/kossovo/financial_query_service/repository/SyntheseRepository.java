package ci.kossovo.financial_query_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ci.kossovo.financial_query_service.projection.model.SyntheseFinanciereDocument;


public interface SyntheseRepository extends MongoRepository<SyntheseFinanciereDocument, String> {}
