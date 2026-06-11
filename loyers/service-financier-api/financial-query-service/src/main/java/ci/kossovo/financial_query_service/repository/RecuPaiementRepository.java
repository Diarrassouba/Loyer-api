package ci.kossovo.financial_query_service.repository;

import ci.kossovo.financial_query_service.projection.model.RecuPaiementDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecuPaiementRepository extends MongoRepository<RecuPaiementDocument, String> {

  List<RecuPaiementDocument> findByContratIdOrderByDatePaiementDesc(String contratId);
}
