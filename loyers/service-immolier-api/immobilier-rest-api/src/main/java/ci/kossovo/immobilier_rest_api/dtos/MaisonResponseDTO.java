package ci.kossovo.immobilier_rest_api.dtos;

import java.util.List;

public record MaisonResponseDTO(
<<<<<<< HEAD
    String maisonId,
    String ilot,
=======
    String id,
    String lot,
>>>>>>> 5dcc91dab149da2c08bdc96b0ad3e50ad5437c96
    String quartier,
    String ville,
    int anneeConstruction,
    // On inclut la liste des appartements pour avoir une vue complète
    List<AppartementResponseDTO> appartements
    // List<DepenseDTO> depenses
    ) {}
