package ci.kossovo.financial_query_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ci.kossovo.financial_query_service.projection.model.RecuPaiementDocument;

@Repository
public interface RecuPaiementRepository extends MongoRepository<RecuPaiementDocument, String> {

    List<RecuPaiementDocument> findByContratIdOrderByDatePaiementDesc(String contratId);
}
