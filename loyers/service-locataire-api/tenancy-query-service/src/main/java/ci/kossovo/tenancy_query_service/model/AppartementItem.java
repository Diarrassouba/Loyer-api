package ci.kossovo.tenancy_query_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppartementItem {
    private String appartementId;
    private String reference;
    private boolean loue = false;

    public AppartementItem(String appartementId, String reference) {
        this.appartementId = appartementId;
        this.reference = reference;
    }
}
