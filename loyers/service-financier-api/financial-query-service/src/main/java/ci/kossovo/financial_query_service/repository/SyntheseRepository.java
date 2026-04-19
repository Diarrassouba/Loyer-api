package ci.kossovo.financial_query_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ci.kossovo.financial_query_service.projection.model.SyntheseFinanciereDocument;

@Repository
public interface SyntheseRepository extends MongoRepository<SyntheseFinanciereDocument, String> {}
