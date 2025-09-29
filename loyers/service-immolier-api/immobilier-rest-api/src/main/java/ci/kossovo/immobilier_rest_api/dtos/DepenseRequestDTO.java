package ci.kossovo.immobilier_rest_api.dtos;

import ci.kossovo.loyer_core_api.enums.immobiliers.TypeDepense;
import java.math.BigDecimal;
import java.time.LocalDate;

<<<<<<< HEAD:loyers/service-immolier-api/immobilier-rest-api/src/main/java/ci/kossovo/immobilier_rest_api/dtos/DepenseDTO.java
public record DepenseDTO(
    String depenseId,
=======
public record DepenseRequestDTO(
    String id,
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96:loyers/service-immolier-api/immobilier-rest-api/src/main/java/ci/kossovo/immobilier_rest_api/dtos/DepenseRequestDTO.java
    BigDecimal montant,
    String description,
    LocalDate date,
    TypeDepense typeDepense,
    String maisonId,
    String appartementId) {}
