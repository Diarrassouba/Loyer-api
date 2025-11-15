package ci.kossovo.raiting_service.repositories;

@Repository
public interface NotationRepository extends JpaRepository<Notation, String> {
  List<Notation> findByLocataireId(String locataireId);
}
