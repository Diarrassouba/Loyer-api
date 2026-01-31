package ci.kossovo.raiting_service.projections.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LocataireValideView {
    @Id
    private String locataireId;

    public LocataireValideView(String locataireId) {
        this.locataireId = locataireId;
    }
}
