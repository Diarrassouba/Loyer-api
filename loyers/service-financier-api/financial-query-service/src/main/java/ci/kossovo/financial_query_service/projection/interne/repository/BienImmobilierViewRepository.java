package ci.kossovo.financial_query_service.projection.interne.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ci.kossovo.financial_query_service.projection.interne.model.BienImmobilierViewDocument;

@Repository
public interface BienImmobilierViewRepository extends MongoRepository<BienImmobilierViewDocument, String> {}
