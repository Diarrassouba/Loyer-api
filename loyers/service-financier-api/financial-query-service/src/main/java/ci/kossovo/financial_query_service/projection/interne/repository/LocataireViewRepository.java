package ci.kossovo.financial_query_service.projection.interne.repository;

import ci.kossovo.financial_query_service.projection.interne.model.LocataireViewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocataireViewRepository extends MongoRepository<LocataireViewDocument, String> {}
