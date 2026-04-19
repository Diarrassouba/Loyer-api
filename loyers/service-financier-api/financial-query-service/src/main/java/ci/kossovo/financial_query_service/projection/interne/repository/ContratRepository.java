package ci.kossovo.financial_query_service.projection.interne.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ci.kossovo.financial_query_service.projection.interne.model.Contrat;

public interface ContratRepository extends MongoRepository<Contrat, String> {}
