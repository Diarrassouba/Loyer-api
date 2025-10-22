package ci.kossovo.locataire_rest_api.models;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Locataire {

    @Id
    private String id = UUID.randomUUID().toString();
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

}
