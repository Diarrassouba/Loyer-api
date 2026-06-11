package ci.kossovo.financial_query_service.projection.interne.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;


public interface BienImmobilierViewRepository extends MongoRepository<BienImmobilierViewDocument, String> {}
