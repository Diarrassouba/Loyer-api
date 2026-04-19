package ci.kossovo.financial_query_service.repository;

import ci.kossovo.financial_query_service.projection.model.TransactionDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findByContratIdOrderByDateDesc(String contratId);
}
