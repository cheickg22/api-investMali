package abdaty_technologie.API_Invest.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import abdaty_technologie.API_Invest.Entity.Utilisateurs;
import abdaty_technologie.API_Invest.Entity.Persons;
import abdaty_technologie.API_Invest.Entity.Divisions;
import abdaty_technologie.API_Invest.Entity.Entreprise;
import abdaty_technologie.API_Invest.Entity.PasswordResetToken;
import abdaty_technologie.API_Invest.Entity.Enum.*;
import abdaty_technologie.API_Invest.constants.Messages;
import abdaty_technologie.API_Invest.dto.requests.LoginRequest;
import abdaty_technologie.API_Invest.dto.requests.RegisterRequest;
import abdaty_technologie.API_Invest.dto.requests.ForgotPasswordRequest;
import abdaty_technologie.API_Invest.dto.requests.ResetPasswordRequest;
import abdaty_technologie.API_Invest.dto.responses.LoginResponse;
import abdaty_technologie.API_Invest.dto.responses.UserAuthResponse;
import abdaty_technologie.API_Invest.repository.UtilisateursRepository;
import abdaty_technologie.API_Invest.repository.PersonsRepository;
import abdaty_technologie.API_Invest.repository.DivisionsRepository;
import abdaty_technologie.API_Invest.repository.EntrepriseRepository;
import abdaty_technologie.API_Invest.repository.PasswordResetTokenRepository;
import abdaty_technologie.API_Invest.service.IAuthService;
import abdaty_technologie.API_Invest.util.JwtUtil;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UtilisateursRepository utilisateursRepository;

    @Autowired
    private PersonsRepository personsRepository;

    @Autowired
    private DivisionsRepository divisionsRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        // Rechercher la personne par email
        Persons person = personsRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException(Messages.PERSON_NOT_FOUND + loginRequest.getEmail()));

        // Trouver le compte utilisateur lié à cette personne
        Utilisateurs utilisateur = utilisateursRepository.findByPersonneId(person.getId())
                .orElseThrow(() -> new BadCredentialsException(Messages.UTILISATEUR_NON_TROUVE));

        // Vérifier le mot de passe (hash) avec auto-migration legacy si en clair
        if (!passwordEncoder.matches(loginRequest.getMotdepasse(), utilisateur.getMotdepasse())) {
            // Cas legacy: ancien mot de passe stocké en clair
            if (loginRequest.getMotdepasse().equals(utilisateur.getMotdepasse())) {
                utilisateur.setMotdepasse(passwordEncoder.encode(loginRequest.getMotdepasse()));
                utilisateursRepository.save(utilisateur);
            } else {
                throw new BadCredentialsException(Messages.MOT_DE_PASSE_INCORRECT);
            }
        }

        // Générer le token JWT avec le rôle de la personne
        String role = "USER"; // basé sur présence d'une personne
        String token = jwtUtil.generateToken(utilisateur.getUtilisateur(), role);

        return new LoginResponse(token, utilisateur.getUtilisateur(), role, person.getNom(), person.getPrenom(), person.getEmail());
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // Déterminer l'identifiant du compte: username explicite ou email par défaut
        String username = (request.getUtilisateur() != null && !request.getUtilisateur().isBlank())
                ? request.getUtilisateur().trim()
                : request.getEmail();

        // Validations de base (unicité)
        if (utilisateursRepository.existsByUtilisateur(username)) {
            throw new RuntimeException("Nom d'utilisateur déjà utilisé");
        }
        if (personsRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (request.getTelephone1() != null && personsRepository.existsByTelephone1(request.getTelephone1())) {
            throw new RuntimeException("Téléphone déjà utilisé");
        }

        // Parsing date
        Date dateNaissance;
        try {
            dateNaissance = new SimpleDateFormat("yyyy-MM-dd").parse(request.getDateNaissance());
        } catch (ParseException e) {
            throw new RuntimeException(Messages.invalidFieldFormat("dateNaissance"));
        }

        // Conversion des enums (en majuscules pour tolérance)
        Nationalites nationalite = Nationalites.valueOf(request.getNationalite().toUpperCase());
        Sexes sexe = Sexes.valueOf(request.getSexe().toUpperCase());
        SituationMatrimoniales situation = SituationMatrimoniales.valueOf(request.getSituationMatrimoniale().toUpperCase());
        Civilites civilite = Civilites.valueOf(request.getCivilite().toUpperCase());
        Roles role = (request.getRole() == null || request.getRole().isBlank()) ? Roles.USER : Roles.valueOf(request.getRole().toUpperCase());
        AntenneAgents antenne = AntenneAgents.valueOf(request.getAntenneAgent().toUpperCase());
        EntrepriseRole entrepriseRole = EntrepriseRole.valueOf(request.getEntrepriseRole().toUpperCase());

        // Création de la personne
        Persons p = new Persons();
        p.setNom(request.getNom());
        p.setPrenom(request.getPrenom());
        p.setEmail(request.getEmail());
        p.setTelephone1(request.getTelephone1());
        p.setTelephone2(request.getTelephone2());
        p.setDateNaissance(dateNaissance);
        p.setLieuNaissance(request.getLieuNaissance());
        p.setLocalite(request.getLocalite());
        p.setEstAutoriser(true); // autorisé par défaut
        p.setNationalite(nationalite);
        p.setEntrepriseRole(entrepriseRole);
        p.setAntenneAgent(antenne);
        p.setSexe(sexe);
        p.setSituationMatrimoniale(situation);
        p.setCivilite(civilite);
        p.setRole(role);

        // Division optionnelle (objet imbriqué avec id)
        if (request.getDivision() != null && request.getDivision().getId() != null && !request.getDivision().getId().isBlank()) {
            Divisions div = divisionsRepository.findById(request.getDivision().getId())
                .orElseThrow(() -> new RuntimeException(Messages.invalidFieldFormat("division.id")));
            p.setDivision(div);
        }

        // Entreprise optionnelle
        if (request.getEntrepriseId() != null && !request.getEntrepriseId().isBlank()) {
            Entreprise ent = entrepriseRepository.findById(request.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException(Messages.ENTERPRISE_NOT_FOUND + request.getEntrepriseId()));
            p.setEntreprise(ent);
        }

        // Persister la personne
        Persons saved = personsRepository.save(p);

        // Créer le compte utilisateur lié à la personne
        Utilisateurs u = new Utilisateurs();
        u.setUtilisateur(username);
        u.setMotdepasse(passwordEncoder.encode(request.getMotdepasse()));
        u.setPersonne(saved);
        utilisateursRepository.save(u);

        // Générer un token et répondre
        String roleStr = "USER";
        String token = jwtUtil.generateToken(u.getUtilisateur(), roleStr);
        return new LoginResponse(token, u.getUtilisateur(), roleStr, saved.getNom(), saved.getPrenom(), saved.getEmail());
    }

    @Override
    @Transactional
    public String requestPasswordReset(ForgotPasswordRequest request) {
        // Trouver la personne par email
        Persons person = personsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email non trouvé"));

        Utilisateurs utilisateur = utilisateursRepository.findByPersonneId(person.getId())
                .orElseThrow(() -> new BadCredentialsException(Messages.UTILISATEUR_NON_TROUVE));

        // Générer un token unique avec expiration
        String token = UUID.randomUUID().toString();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30); // valide 30 minutes
        Date expiresAt = cal.getTime();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setExpiresAt(expiresAt);
        prt.setUtilisateur(utilisateur);
        passwordResetTokenRepository.save(prt);

        // En production, on enverrait un email avec ce token. Ici on le retourne.
        return token;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Token de réinitialisation invalide"));

        // Vérifier expiration
        if (prt.getExpiresAt().before(new Date())) {
            passwordResetTokenRepository.deleteByToken(request.getToken());
            throw new BadCredentialsException("Token de réinitialisation expiré");
        }

        Utilisateurs utilisateur = prt.getUtilisateur();
        utilisateur.setMotdepasse(passwordEncoder.encode(request.getNouveauMotdepasse()));
        utilisateursRepository.save(utilisateur);

        // Invalider le token après usage
        passwordResetTokenRepository.deleteByToken(request.getToken());
    }

    @Override
    public UserAuthResponse getUserAuthInfo(String userId) {
        Utilisateurs utilisateur = utilisateursRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(Messages.UTILISATEUR_NON_TROUVE));
        
        return createUserAuthResponse(utilisateur);
    }

    @Override
    public List<UserAuthResponse> getAllUsersInfo() {
        return utilisateursRepository.findAll().stream()
                .map(this::createUserAuthResponse)
                .collect(Collectors.toList());
    }
    
    private UserAuthResponse createUserAuthResponse(Utilisateurs utilisateur) {
        UserAuthResponse response = new UserAuthResponse();
        response.setUserId(utilisateur.getId());
        response.setUtilisateur(utilisateur.getUtilisateur());
        
        if (utilisateur.getPersonne() != null) {
            response.setNom(utilisateur.getPersonne().getNom());
            response.setPrenom(utilisateur.getPersonne().getPrenom());
            response.setEmail(utilisateur.getPersonne().getEmail());
            response.setEstAutoriser(utilisateur.getPersonne().getEstAutoriser());
            response.setRole("USER");
            
            // Informations de division si disponibles
            if (utilisateur.getPersonne().getDivision() != null) {
                response.setDivisionNom(utilisateur.getPersonne().getDivision().getNom());
                response.setDivisionType(utilisateur.getPersonne().getDivision().getDivisionType().toString());
            }
        } else {
            response.setRole("ADMIN");
            response.setEstAutoriser(true);
        }
        
        return response;
    }
}
