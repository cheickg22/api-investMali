package abdaty_technologie.API_Invest.dto.request;

import abdaty_technologie.API_Invest.Entity.Enum.EntrepriseRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ParticipantRequest {

    // L'ID du membre de la creation de l'entreprise
    @NotBlank
    public String personId;

    //Role du membre de l'entreprise (FONDATEUR, ASSOCIE, GERANT)
    @NotBlank
    public EntrepriseRole role;

    //Le part de pourcentage du membre
    @NotBlank
    @DecimalMin(value = "0.00")
    public BigDecimal pourcentageParts;

    //La date de debut de l'affectation du membre a l'entreprise
    @NotBlank
    public LocalDate dateDebut;

    // La date de fin de fin du membre a l'entreprise
    @NotBlank
    public LocalDate dateFin;
}
