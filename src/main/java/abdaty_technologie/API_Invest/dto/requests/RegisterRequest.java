package abdaty_technologie.API_Invest.dto.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import abdaty_technologie.API_Invest.constants.ValidationMessages;

@Data
public class RegisterRequest {
    // Informations personnelles de base
    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank(message = ValidationMessages.PERSON_EMAIL_REQUIRED)
    @Email(message = ValidationMessages.PERSON_EMAIL_INVALID)
    private String email;

    @NotBlank
    private String telephone1;
    
    // Optionnel
    private String telephone2;

    // yyyy-MM-dd
    @NotBlank
    private String dateNaissance;

    @NotBlank
    private String lieuNaissance;

    // Optionnels
    private String localite;

    // Enums (attendus en STRING, ex: "MALIEN", "MASCULIN", etc.)
    @NotBlank
    private String nationalite;

    @NotBlank
    private String sexe;

    @NotBlank
    private String situationMatrimoniale;

    @NotBlank
    private String civilite;

    @NotBlank
    private String antenneAgent;

    @NotBlank
    private String entrepriseRole;

    // Rôle applicatif (par défaut USER si vide côté service)
    private String role;

    // Identifiants de rattachement optionnels
    private DivisionRef division;
    private String entrepriseId;

    // Compte utilisateur (optionnel) — si absent, l'email sera utilisé comme identifiant
    private String utilisateur;

    @NotBlank
    private String motdepasse;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DivisionRef {
        private String id;
    }
}
