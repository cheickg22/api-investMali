package abdaty_technologie.API_Invest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import abdaty_technologie.API_Invest.Entity.Entreprise;
import abdaty_technologie.API_Invest.Entity.Divisions;
import abdaty_technologie.API_Invest.Entity.Enum.DivisionType;
import abdaty_technologie.API_Invest.dto.request.EntrepriseRequest;
import abdaty_technologie.API_Invest.dto.response.EntrepriseResponse;
import abdaty_technologie.API_Invest.dto.response.MembreResponse;
import abdaty_technologie.API_Invest.dto.request.BanEntrepriseRequest;
import abdaty_technologie.API_Invest.dto.request.UpdateEntrepriseRequest;
import abdaty_technologie.API_Invest.service.EntrepriseService;
import abdaty_technologie.API_Invest.exception.NotFoundException;
import abdaty_technologie.API_Invest.Entity.EntrepriseMembre;
import abdaty_technologie.API_Invest.repository.EntrepriseRepository;
import abdaty_technologie.API_Invest.repository.EntrepriseMembreRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour les opérations sur les entreprises.
 *
 * Expose:
 * Les endpoints sont automatiquement préfixés par /api/v1 via spring.mvc.servlet.path.
 * - POST /entreprises: création d'une entreprise avec génération automatique de la référence
 * - GET  /entreprises: liste paginée (et triable) des entreprises, avec filtre optionnel par divisionCode
 */
@RestController
@RequestMapping("/entreprises")
public class EntrepriseController {

    @Autowired
    private EntrepriseService entrepriseService;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private EntrepriseMembreRepository entrepriseMembreRepository;

    /**
     * Crée une entreprise.
     * - Valide la requête (@Valid)
     * - Délègue au service qui génère la référence (CE-YYYY-MM-DD-#####)
     * - Retourne une réponse épurée (EntrepriseResponse)
     */
    @PostMapping
    public ResponseEntity<EntrepriseResponse> Entreprise(@RequestBody @Valid EntrepriseRequest request) {
        Entreprise created = entrepriseService.createEntreprise(request);
        return ResponseEntity.ok(toResponse(created));
    }

    /**
     * Liste paginée des entreprises.
     * - Paramètres Spring Data: page, size, sort
     * - Filtre optionnel par code de division (divisionCode)
     */
    @GetMapping
    public ResponseEntity<Page<EntrepriseResponse>> listEntreprises(
            @RequestParam(value = "divisionCode", required = false) String divisionCode,
            Pageable pageable) {
        Page<Entreprise> page = entrepriseService.listEntreprises(divisionCode, pageable);
        // Récupérer les IDs des entreprises de la page
        List<Entreprise> entreprises = page.getContent();
        List<String> ids = entreprises.stream().map(Entreprise::getId).toList();
        // Batch fetch des membres + personne pour éviter N+1 et lazy
        Map<String, List<EntrepriseMembre>> membresByEntreprise = ids.isEmpty() ? Map.of() :
                entrepriseMembreRepository.findByEntrepriseIdsWithPersonne(ids)
                    .stream()
                    .collect(Collectors.groupingBy(em -> em.getEntreprise().getId()));

        Page<EntrepriseResponse> mapped = page.map(e -> {
            EntrepriseResponse r = toResponseShallow(e);
            List<EntrepriseMembre> ems = membresByEntreprise.get(e.getId());
            if (ems != null) {
                r.membres = ems.stream().map(em -> {
                    MembreResponse mr = new MembreResponse();
                    if (em.getPersonne() != null) {
                        mr.personId = em.getPersonne().getId();
                        mr.nom = em.getPersonne().getNom();
                        mr.prenom = em.getPersonne().getPrenom();
                    }
                    mr.role = em.getRole();
                    mr.pourcentageParts = em.getPourcentageParts();
                    mr.dateDebut = em.getDateDebut();
                    mr.dateFin = em.getDateFin();
                    return mr;
                }).toList();
            }
            return r;
        });
        return ResponseEntity.ok(mapped);
    }

    /**
     * Liste paginée des entreprises bannies.
     */
    @GetMapping("/bannis")
    public ResponseEntity<Page<EntrepriseResponse>> listEntreprisesBannies(Pageable pageable) {
        Page<Entreprise> page = entrepriseService.listBanned(pageable);
        // on inclut les membres comme pour la liste standard
        List<Entreprise> entreprises = page.getContent();
        List<String> ids = entreprises.stream().map(Entreprise::getId).toList();
        Map<String, List<EntrepriseMembre>> membresByEntreprise = ids.isEmpty() ? Map.of() :
                entrepriseMembreRepository.findByEntrepriseIdsWithPersonne(ids)
                    .stream()
                    .collect(Collectors.groupingBy(em -> em.getEntreprise().getId()));
        Page<EntrepriseResponse> mapped = page.map(e -> {
            EntrepriseResponse r = toResponseShallow(e);
            List<EntrepriseMembre> ems = membresByEntreprise.get(e.getId());
            if (ems != null) {
                r.membres = ems.stream().map(em -> {
                    MembreResponse mr = new MembreResponse();
                    if (em.getPersonne() != null) {
                        mr.personId = em.getPersonne().getId();
                        mr.nom = em.getPersonne().getNom();
                        mr.prenom = em.getPersonne().getPrenom();
                    }
                    mr.role = em.getRole();
                    mr.pourcentageParts = em.getPourcentageParts();
                    mr.dateDebut = em.getDateDebut();
                    mr.dateFin = em.getDateFin();
                    return mr;
                }).toList();
            }
            return r;
        });
        return ResponseEntity.ok(mapped);
    }

    /**
     * Récupère une entreprise par son identifiant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> getEntrepriseById(@PathVariable String id) {
        // Charger avec fetch join pour inclure membres et personnes
        Entreprise e = entrepriseRepository.findByIdWithMembres(id)
            .orElseThrow(() -> new NotFoundException("Entreprise introuvable: " + id));
        return ResponseEntity.ok(toResponse(e));
    }

    /**
     * Bannir une entreprise (avec motif obligatoire).
     */
    @PostMapping("/{id}/ban")
    public ResponseEntity<EntrepriseResponse> ban(@PathVariable String id, @RequestBody @Valid BanEntrepriseRequest req) {
        Entreprise e = entrepriseService.ban(id, req);
        return ResponseEntity.ok(toResponseShallow(e));
    }

    /**
     * Dé-bannir une entreprise.
     */
    @PostMapping("/{id}/unban")
    public ResponseEntity<EntrepriseResponse> unban(@PathVariable String id) {
        Entreprise e = entrepriseService.unban(id);
        return ResponseEntity.ok(toResponseShallow(e));
    }

    /**
     * Mise à jour partielle d'une entreprise.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EntrepriseResponse> update(@PathVariable String id, @RequestBody @Valid UpdateEntrepriseRequest req) {
        Entreprise e = entrepriseService.updateEntreprise(id, req);
        return ResponseEntity.ok(toResponseShallow(e));
    }

    /**
     * Récupère les membres (personnes liées) d'une entreprise.
     */
    @GetMapping("/{id}/membres")
    public ResponseEntity<List<MembreResponse>> getMembres(@PathVariable String id) {
        // Vérifier l'existence pour un 404 propre
        if (!entrepriseRepository.existsById(id)) {
            throw new NotFoundException("Entreprise introuvable: " + id);
        }
        // Charger avec fetch join la personne pour éviter les proxies
        List<EntrepriseMembre> membres = entrepriseMembreRepository.findByEntrepriseIdWithPersonne(id);
        List<MembreResponse> out = membres.stream().map(em -> {
            MembreResponse mr = new MembreResponse();
            if (em.getPersonne() != null) {
                mr.personId = em.getPersonne().getId();
                mr.nom = em.getPersonne().getNom();
                mr.prenom = em.getPersonne().getPrenom();
            }
            mr.role = em.getRole();
            mr.pourcentageParts = em.getPourcentageParts();
            mr.dateDebut = em.getDateDebut();
            mr.dateFin = em.getDateFin();
            return mr;
        }).toList();
        return ResponseEntity.ok(out);
    }

    /**
     * Mappe une entité Entreprise vers une réponse API minimale.
     * - Projette les informations de base
     * - Remonte la hiérarchie de Divisions (QUARTIER -> ... -> REGION) via le parent
     */
    private EntrepriseResponse toResponse(Entreprise e) {
        EntrepriseResponse r = new EntrepriseResponse();
        r.id = e.getId();
        r.reference = e.getReference();
        r.nom = e.getNom();
        r.sigle = e.getSigle();
        r.typeEntreprise = e.getTypeEntreprise();
        r.statutCreation = e.getStatutCreation();
        r.etapeValidation = e.getEtapeValidation();
        r.formeJuridique = e.getFormeJuridique();
        r.domaineActivite = e.getDomaineActivite();
        r.statutSociete = e.getStatutSociete();

        Divisions d = e.getDivision();
        if (d != null) {
            r.divisionCode = d.getCode();
            r.divisionNom = d.getNom();

            // Remonter la hiérarchie parentale jusqu'à la racine (REGION)
            Divisions cursor = d;
            while (cursor != null) {
                DivisionType type = cursor.getDivisionType();
                if (type != null) {
                    switch (type) {
                        case REGION -> { 
                            // Division de type REGION
                            r.regionCode = cursor.getCode(); 
                            r.regionNom = cursor.getNom(); 
                        }
                        case CERCLE -> { 
                            // Division de type CERCLE
                            r.cercleCode = cursor.getCode(); 
                            r.cercleNom = cursor.getNom(); 
                        }
                        case ARRONDISSEMENT -> { 
                            // Division de type ARRONDISSEMENT
                            r.arrondissementCode = cursor.getCode(); 
                            r.arrondissementNom = cursor.getNom(); 
                        }
                        case COMMUNE -> { 
                            // Division de type COMMUNE
                            r.communeCode = cursor.getCode(); 
                            r.communeNom = cursor.getNom(); 
                        }
                        case QUARTIER -> { 
                            // Division de type QUARTIER
                            r.quartierCode = cursor.getCode(); 
                            r.quartierNom = cursor.getNom(); 
                        }
                        default -> {}
                    }
                }
                cursor = cursor.getParent();
            }
        }

        r.creation = e.getCreation();
        r.modification = e.getModification();
        r.banni = e.getBanni();
        r.motifBannissement = e.getMotifBannissement();
        r.dateBannissement = e.getDateBannissement();

        // Map des membres (personnes liées) avec rôle et parts
        if (e.getMembres() != null) {
            r.membres = e.getMembres().stream().map(em -> {
                MembreResponse mr = new MembreResponse();
                if (em.getPersonne() != null) {
                    mr.personId = em.getPersonne().getId();
                    mr.nom = em.getPersonne().getNom();
                    mr.prenom = em.getPersonne().getPrenom();
                }
                mr.role = em.getRole();
                mr.pourcentageParts = em.getPourcentageParts();
                mr.dateDebut = em.getDateDebut();
                mr.dateFin = em.getDateFin();
                return mr;
            }).toList();
        }
        return r;
    }

    // Mapping léger sans membres (utilisé pour la liste paginée)
    private EntrepriseResponse toResponseShallow(Entreprise e) {
        EntrepriseResponse r = new EntrepriseResponse();
        r.id = e.getId();
        r.reference = e.getReference();
        r.nom = e.getNom();
        r.sigle = e.getSigle();
        r.typeEntreprise = e.getTypeEntreprise();
        r.statutCreation = e.getStatutCreation();
        r.etapeValidation = e.getEtapeValidation();
        r.formeJuridique = e.getFormeJuridique();
        r.domaineActivite = e.getDomaineActivite();

        Divisions d = e.getDivision();
        if (d != null) {
            r.divisionCode = d.getCode();
            r.divisionNom = d.getNom();

            Divisions cursor = d;
            while (cursor != null) {
                DivisionType type = cursor.getDivisionType();
                if (type != null) {
                    switch (type) {
                        case REGION -> { r.regionCode = cursor.getCode(); r.regionNom = cursor.getNom(); }
                        case CERCLE -> { r.cercleCode = cursor.getCode(); r.cercleNom = cursor.getNom(); }
                        case ARRONDISSEMENT -> { r.arrondissementCode = cursor.getCode(); r.arrondissementNom = cursor.getNom(); }
                        case COMMUNE -> { r.communeCode = cursor.getCode(); r.communeNom = cursor.getNom(); }
                        case QUARTIER -> { r.quartierCode = cursor.getCode(); r.quartierNom = cursor.getNom(); }
                        default -> {}
                    }
                }
                cursor = cursor.getParent();
            }
        }
        r.creation = e.getCreation();
        r.modification = e.getModification();
        r.banni = e.getBanni();
        r.motifBannissement = e.getMotifBannissement();
        r.dateBannissement = e.getDateBannissement();
        return r;
    }
}
