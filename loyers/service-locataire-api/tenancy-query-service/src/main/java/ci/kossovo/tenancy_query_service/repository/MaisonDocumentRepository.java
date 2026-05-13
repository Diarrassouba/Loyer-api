package ci.kossovo.tenancy_query_service.repository;

import ci.kossovo.tenancy_query_service.model.MaisonDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaisonDocumentRepository extends MongoRepository<MaisonDocument, String> {

  // Requête MongoDB pour trouver la maison qui contient un appartement spécifique
  // Equivalent à : db.maisons_completes.findOne({"appartements.appartementId": "..."})
  Optional<MaisonDocument> findByAppartementsAppartementId(String appartementId);
}
