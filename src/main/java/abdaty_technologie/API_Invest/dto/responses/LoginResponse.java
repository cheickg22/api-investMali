package abdaty_technologie.API_Invest.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private String utilisateur;
    private String role;
    private String nom;
    private String prenom;
    private String email;
    
    public LoginResponse(String token, String utilisateur, String role, String nom, String prenom, String email) {
        this.token = token;
        this.utilisateur = utilisateur;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
}
