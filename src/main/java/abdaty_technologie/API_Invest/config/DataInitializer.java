package abdaty_technologie.API_Invest.config;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import abdaty_technologie.API_Invest.Entity.Divisions;
import abdaty_technologie.API_Invest.Entity.Entreprise;
import abdaty_technologie.API_Invest.Entity.Paiement;
import abdaty_technologie.API_Invest.Entity.Persons;
import abdaty_technologie.API_Invest.Entity.Utilisateurs;
import abdaty_technologie.API_Invest.Entity.Enum.AntenneAgents;
import abdaty_technologie.API_Invest.Entity.Enum.Civilites;
import abdaty_technologie.API_Invest.Entity.Enum.DivisionType;
import abdaty_technologie.API_Invest.Entity.Enum.Nationalites;
import abdaty_technologie.API_Invest.Entity.Enum.Roles;
import abdaty_technologie.API_Invest.Entity.Enum.Sexes;
import abdaty_technologie.API_Invest.Entity.Enum.SituationMatrimoniales;
import abdaty_technologie.API_Invest.Entity.Enum.DomaineActivites;
import abdaty_technologie.API_Invest.Entity.Enum.EtapeValidation;
import abdaty_technologie.API_Invest.Entity.Enum.FormeJuridique;
import abdaty_technologie.API_Invest.Entity.Enum.StatutCreation;
import abdaty_technologie.API_Invest.Entity.Enum.StatutPaiement;
import abdaty_technologie.API_Invest.Entity.Enum.TypeEntreprise;
import abdaty_technologie.API_Invest.Entity.Enum.TypePaiement;
import abdaty_technologie.API_Invest.Entity.Enum.EntrepriseRole;
import abdaty_technologie.API_Invest.repository.DivisionsRepository;
import abdaty_technologie.API_Invest.repository.EntrepriseRepository;
import abdaty_technologie.API_Invest.repository.PaiementRepository;
import abdaty_technologie.API_Invest.repository.PersonsRepository;
import abdaty_technologie.API_Invest.repository.UtilisateursRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PersonsRepository personsRepository;
    
    @Autowired
    private UtilisateursRepository utilisateursRepository;
    
    @Autowired
    private DivisionsRepository divisionsRepository;
    
    @Autowired
    private PaiementRepository paiementRepository;
    
    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si les données par défaut existent déjà
        boolean hasDefaultUsers = utilisateursRepository.findByUtilisateur("admin").isPresent();
        boolean hasDefaultEnterprises = entrepriseRepository.existsByReference("ENT-TECH-001");
        
        if (!hasDefaultUsers) {
            System.out.println("Création des utilisateurs par défaut...");
            createDefaultUsers();
        } else {
            System.out.println("Utilisateurs par défaut déjà présents, pas de création nécessaire.");
        }
        
        if (!hasDefaultEnterprises) {
            System.out.println("Création des entreprises de test...");
            createSampleEnterprises();
        } else {
            System.out.println("Entreprises par défaut déjà présentes, pas de création nécessaire.");
        }
        
        // Les paiements peuvent être créés à chaque démarrage s'ils n'existent pas
        long paiementCount = paiementRepository.count();
        if (paiementCount == 0) {
            System.out.println("Création des paiements de test...");
            createSamplePayments();
        } else {
            System.out.println("Paiements déjà présents (" + paiementCount + "), pas de création nécessaire.");
        }
    }

    private void createDefaultUsers() {
        System.out.println("Début de la création des utilisateurs par défaut...");
        
        // Récupérer la hiérarchie complète des divisions (région -> cercle -> arrondissement -> commune -> quartier)
        final Divisions koulikoroRegion = getKoulikoroRegion();
        
        if (koulikoroRegion == null) {
            System.err.println("ERREUR: Impossible de créer les utilisateurs car aucune région KOULIKORO n'a été trouvée dans la base de données.");
            System.err.println("Veuillez d'abord importer les données des divisions avant de créer des utilisateurs.");
            return;
        }
        
        final Divisions cercle = getCercleFromRegion(koulikoroRegion);
        final Divisions arrondissement = getArrondissementFromCercle(cercle);
        final Divisions commune = getCommuneFromArrondissement(arrondissement);
        final Divisions quartier = getQuartierFromCommune(commune);
        
        // Vérifier si on a une hiérarchie valide
        if (cercle == null || arrondissement == null || commune == null) {
            System.err.println("ERREUR: La hiérarchie des divisions est incomplète.");
            System.err.println("Assurez-vous que la base de données contient au moins une région, un cercle, un arrondissement et une commune valides.");
            return;
        }
        
        // S'assurer que toute la hiérarchie est correctement définie
        if (quartier != null) {
            // Si on a trouvé un quartier, s'assurer que sa hiérarchie parente est correcte
            quartier.setParent(commune);
            divisionsRepository.save(quartier);
        }
        
        if (commune != null) {
            commune.setParent(arrondissement);
            divisionsRepository.save(commune);
        }
        
        if (arrondissement != null) {
            arrondissement.setParent(cercle);
            divisionsRepository.save(arrondissement);
        }
        
        if (cercle != null) {
            cercle.setParent(koulikoroRegion);
            divisionsRepository.save(cercle);
        }

        // Utiliser le niveau le plus profond disponible (quartier > commune > arrondissement > cercle > région)
        Divisions defaultDivision = quartier != null ? quartier : 
                                   commune != null ? commune :
                                   arrondissement != null ? arrondissement :
                                   cercle != null ? cercle : koulikoroRegion;

        // Créer une personne administrateur
        Persons adminPerson = new Persons();
        adminPerson.setNom("Admin");
        adminPerson.setPrenom("Système");
        adminPerson.setEmail("mdz.dev54@gmail.com");
        adminPerson.setTelephone1("+223 70 00 00 00");
        adminPerson.setDateNaissance(new Date());
        adminPerson.setLieuNaissance("Bamako");
        adminPerson.setLocalite("Près de la Grande Mosquée, Avenue Modibo Keita");
        adminPerson.setEstAutoriser(true);
        adminPerson.setNationalite(Nationalites.MALIENNE);
        adminPerson.setEntrepriseRole(EntrepriseRole.FONDATEUR);
        adminPerson.setAntenneAgent(AntenneAgents.BAMAKO);
        adminPerson.setSexe(Sexes.MASCULIN);
        adminPerson.setSituationMatrimoniale(SituationMatrimoniales.CELIBATAIRE);
        adminPerson.setCivilite(Civilites.MONSIEUR);
        adminPerson.setRole(Roles.SUPER_ADMIN);
        adminPerson.setDivision(defaultDivision);
        
        // Sauvegarder la personne
        adminPerson = personsRepository.save(adminPerson);

        // Créer l'utilisateur admin
        Utilisateurs adminUser = new Utilisateurs();
        adminUser.setUtilisateur("admin");
        adminUser.setMotdepasse("admin123"); // En production, utiliser un hash
        adminUser.setPersonne(adminPerson);
        
        utilisateursRepository.save(adminUser);

        // Créer une personne utilisateur normal avec la même division
        Persons userPerson = new Persons();
        userPerson.setNom("Utilisateur");
        userPerson.setPrenom("Test");
        userPerson.setEmail("adoukhanse@gmail.com");
        userPerson.setTelephone1("+223 70 11 11 11");
        userPerson.setDateNaissance(new Date());
        userPerson.setLieuNaissance("Bamako");
        userPerson.setLocalite("2ème rue Mobido Keita, près du marché central");
        userPerson.setEstAutoriser(true);
        userPerson.setNationalite(Nationalites.MALIENNE);
        userPerson.setEntrepriseRole(EntrepriseRole.FONDATEUR);
        userPerson.setAntenneAgent(AntenneAgents.BAMAKO);
        userPerson.setSexe(Sexes.FEMININ);
        userPerson.setSituationMatrimoniale(SituationMatrimoniales.CELIBATAIRE);
        userPerson.setCivilite(Civilites.MADAME);
        userPerson.setRole(Roles.USER);
        userPerson.setDivision(defaultDivision);
        
        // Sauvegarder la personne
        userPerson = personsRepository.save(userPerson);

        // Créer l'utilisateur normal
        Utilisateurs normalUser = new Utilisateurs();
        normalUser.setUtilisateur("user");
        normalUser.setMotdepasse("user123"); // En production, utiliser un hash
        normalUser.setPersonne(userPerson);
        
        utilisateursRepository.save(normalUser);

        System.out.println("Utilisateurs par défaut créés:");
        System.out.println("- Admin: admin/admin123");
        System.out.println("- User: user/user123");
        
        // Afficher la division utilisée et la hiérarchie complète
        if (defaultDivision != null) {
            System.out.println("\n=== Hiérarchie des divisions assignée ===");
            printDivisionHierarchy(defaultDivision);
        }
    }

    private void printDivisionHierarchy(Divisions division) {
        if (division == null) return;
        
        System.out.println("\nDivision assignée: " + division.getNom() + " (" + division.getDivisionType() + ")");
        
        // Remonter la hiérarchie pour afficher les parents
        Divisions current = division;
        while (current != null) {
            System.out.println("- " + current.getDivisionType() + ": " + 
                             current.getNom() + " (ID: " + current.getId() + ")");
            current = current.getParent();
        }
    }
    
    private Divisions getKoulikoroRegion() {
        // Rechercher spécifiquement la région KOULIKORO
        Divisions koulikoroRegion = divisionsRepository.findAll().stream()
                .filter(d -> d.getDivisionType() == DivisionType.REGION && "KOULIKORO".equals(d.getNom()))
                .findFirst()
                .orElse(null);
        
        if (koulikoroRegion == null) {
            System.out.println("Avertissement: Aucune région KOULIKORO trouvée dans la base de données");
        }
        
        return koulikoroRegion;
    }
    
    private Divisions getCercleFromRegion(Divisions region) {
        if (region == null) return null;
        Divisions cercle = divisionsRepository.findAll().stream()
                .filter(d -> d.getDivisionType() == DivisionType.CERCLE && 
                           d.getParent() != null && d.getParent().getId().equals(region.getId()))
                .findFirst()
                .orElse(null);
        
        if (cercle != null) {
            System.out.println("Cercle trouvé: " + cercle.getNom() + " (ID: " + cercle.getId() + ")");
        } else {
            System.out.println("Aucun cercle trouvé pour la région: " + region.getNom());
        }
        return cercle;
    }
    
    private Divisions getArrondissementFromCercle(Divisions cercle) {
        if (cercle == null) return null;
        Divisions arrondissement = divisionsRepository.findAll().stream()
                .filter(d -> d.getDivisionType() == DivisionType.ARRONDISSEMENT && 
                           d.getParent() != null && d.getParent().getId().equals(cercle.getId()))
                .findFirst()
                .orElse(null);
        
        if (arrondissement != null) {
            System.out.println("Arrondissement trouvé: " + arrondissement.getNom() + " (ID: " + arrondissement.getId() + ")");
        } else {
            System.out.println("Aucun arrondissement trouvé pour le cercle: " + cercle.getNom());
        }
        return arrondissement;
    }
    
    private Divisions getCommuneFromArrondissement(Divisions arrondissement) {
        if (arrondissement == null) return null;
        Divisions commune = divisionsRepository.findAll().stream()
                .filter(d -> d.getDivisionType() == DivisionType.COMMUNE && 
                           d.getParent() != null && d.getParent().getId().equals(arrondissement.getId()))
                .findFirst()
                .orElse(null);
        
        if (commune != null) {
            System.out.println("Commune trouvée: " + commune.getNom() + " (ID: " + commune.getId() + ")");
        } else {
            System.out.println("Aucune commune trouvée pour l'arrondissement: " + arrondissement.getNom());
        }
        return commune;
    }
    
    private Divisions getQuartierFromCommune(Divisions commune) {
        if (commune == null) return null;
        Divisions quartier = divisionsRepository.findAll().stream()
                .filter(d -> d.getDivisionType() == DivisionType.QUARTIER && 
                           d.getParent() != null && d.getParent().getId().equals(commune.getId()))
                .findFirst()
                .orElse(null);
        
        if (quartier != null) {
            System.out.println("Quartier trouvé: " + quartier.getNom() + " (ID: " + quartier.getId() + ")");
        } else {
            System.out.println("Aucun quartier trouvé pour la commune: " + commune.getNom());
        }
        return quartier;
    }

    private void createSamplePayments() {
        // Récupérer les personnes créées
        Persons adminPerson = personsRepository.findAll().stream()
                .filter(p -> p.getEmail().equals("admin@api-invest.ml"))
                .findFirst()
                .orElse(null);
        
        Persons userPerson = personsRepository.findAll().stream()
                .filter(p -> p.getEmail().equals("user@api-invest.ml"))
                .findFirst()
                .orElse(null);

        // Récupérer ou créer l'entreprise
        Entreprise entreprise1 = getOrCreateEnterprise("ENT-TECH-001");

        if (adminPerson != null && userPerson != null) {
            // Paiement 1 - Mobile Money validé
            Paiement paiement1 = new Paiement();
            paiement1.setTypePaiement(TypePaiement.MOBILE_MONEY);
            paiement1.setMontant(new BigDecimal("75000"));
            paiement1.setPersonne(adminPerson);
            paiement1.setDescription("Investissement initial - Mobile Money");
            paiement1.setNumeroTelephone("+223 70 12 34 56");
            paiement1.setStatut(StatutPaiement.VALIDE);
            paiement1.setReferenceTransaction("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paiementRepository.save(paiement1);

            // Paiement 2 - Virement bancaire en attente avec entreprise
            Paiement paiement2 = new Paiement();
            paiement2.setTypePaiement(TypePaiement.VIREMENT_BANCAIRE);
            paiement2.setMontant(new BigDecimal("150000"));
            paiement2.setPersonne(adminPerson);
            if (entreprise1 != null) {
                paiement2.setEntreprise(entreprise1); // Associer à l'entreprise
            }
            paiement2.setDescription("Investissement complémentaire - Virement");
            paiement2.setNumeroCompte("ML12 1234 5678 9012 3456 7890");
            paiement2.setStatut(StatutPaiement.EN_ATTENTE);
            paiement2.setReferenceTransaction("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paiementRepository.save(paiement2);

            // Paiement 3 - Orange Money validé
            Paiement paiement3 = new Paiement();
            paiement3.setTypePaiement(TypePaiement.ORANGE_MONEY);
            paiement3.setMontant(new BigDecimal("25000"));
            paiement3.setPersonne(userPerson);
            paiement3.setDescription("Premier investissement utilisateur");
            paiement3.setNumeroTelephone("+223 65 98 76 54");
            paiement3.setStatut(StatutPaiement.VALIDE);
            paiement3.setReferenceTransaction("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paiementRepository.save(paiement3);

            // Paiement 4 - Espèces refusé
            Paiement paiement4 = new Paiement();
            paiement4.setTypePaiement(TypePaiement.ESPECES);
            paiement4.setMontant(new BigDecimal("10000"));
            paiement4.setPersonne(userPerson);
            paiement4.setDescription("Paiement en espèces");
            paiement4.setStatut(StatutPaiement.REFUSE);
            paiement4.setReferenceTransaction("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paiementRepository.save(paiement4);
        }

        System.out.println("Paiements de test créés avec succès !");
    }

    private void createSampleEnterprises() {
        // Utiliser getOrCreateEnterprise pour éviter les doublons
        Entreprise entreprise1 = getOrCreateEnterprise("ENT-TECH-001");
        Entreprise entreprise2 = getOrCreateEnterprise("ENT-COM-002");

        System.out.println("Initialisation des entreprises terminée !");
        if (entreprise1 != null) {
            System.out.println("- " + entreprise1.getNom() + " (ID: " + entreprise1.getId() + ")");
        }
        if (entreprise2 != null) {
            System.out.println("- " + entreprise2.getNom() + " (ID: " + entreprise2.getId() + ")");
        }
    }

    private Entreprise getOrCreateEnterprise(String reference) {
        // Vérifier si l'entreprise existe déjà par référence
        Entreprise existingEntreprise = entrepriseRepository.findAll().stream()
                .filter(e -> e.getReference().equals(reference))
                .findFirst()
                .orElse(null);

        if (existingEntreprise != null) {
            System.out.println("Entreprise existante trouvée par référence: " + existingEntreprise.getNom() + " (ID: " + existingEntreprise.getId() + ")");
            return existingEntreprise;
        }

        // Vérifier aussi par nom pour éviter les doublons
        final String nomEntreprise;
        if (reference.equals("ENT-TECH-001")) {
            nomEntreprise = "Abdaty Technologies SARL";
        } else if (reference.equals("ENT-COM-002")) {
            nomEntreprise = "Mali Commerce International SA";
        } else {
            nomEntreprise = "";
        }

        if (!nomEntreprise.isEmpty()) {
            Entreprise existingByName = entrepriseRepository.findAll().stream()
                    .filter(e -> e.getNom().equals(nomEntreprise))
                    .findFirst()
                    .orElse(null);

            if (existingByName != null) {
                System.out.println("Entreprise existante trouvée par nom: " + existingByName.getNom() + " (ID: " + existingByName.getId() + ")");
                // Mettre à jour la référence si elle est différente
                if (!existingByName.getReference().equals(reference)) {
                    existingByName.setReference(reference);
                    existingByName = entrepriseRepository.save(existingByName);
                    System.out.println("Référence mise à jour pour l'entreprise: " + reference);
                }
                return existingByName;
            }
        }

        // Créer l'entreprise si elle n'existe pas
        System.out.println("Création de l'entreprise avec référence: " + reference);
        Divisions koulikoroRegion = getKoulikoroRegion();
        
        if (koulikoroRegion == null) {
            System.out.println("Impossible de créer l'entreprise - aucune région disponible");
            return null;
        }
        
        System.out.println("Utilisation de la région: " + koulikoroRegion.getNom() + " pour l'entreprise " + reference);

        Entreprise nouvelleEntreprise = new Entreprise();
        
        if (reference.equals("ENT-TECH-001")) {
            nouvelleEntreprise.setReference(reference);
            nouvelleEntreprise.setNom("Abdaty Technologies SARL");
            nouvelleEntreprise.setSigle("ABDTECH");
            nouvelleEntreprise.setAdresseDifferentIdentite(false);
            nouvelleEntreprise.setExtraitJudiciaire(true);
            nouvelleEntreprise.setAutorisationGerant(true);
            nouvelleEntreprise.setAutorisationExercice(true);
            nouvelleEntreprise.setImportExport(false);
            nouvelleEntreprise.setTypeEntreprise(TypeEntreprise.SOCIETE);
            nouvelleEntreprise.setStatutCreation(StatutCreation.EN_COURS);
            nouvelleEntreprise.setEtapeValidation(EtapeValidation.ACCUEIL);
            nouvelleEntreprise.setFormeJuridique(FormeJuridique.SARL);
            nouvelleEntreprise.setDomaineActivite(DomaineActivites.NTIC);
            nouvelleEntreprise.setDivision(koulikoroRegion);
        } else if (reference.equals("ENT-COM-002")) {
            nouvelleEntreprise.setReference(reference);
            nouvelleEntreprise.setNom("Mali Commerce International SA");
            nouvelleEntreprise.setSigle("MCISA");
            nouvelleEntreprise.setAdresseDifferentIdentite(true);
            nouvelleEntreprise.setExtraitJudiciaire(true);
            nouvelleEntreprise.setAutorisationGerant(true);
            nouvelleEntreprise.setAutorisationExercice(true);
            nouvelleEntreprise.setImportExport(true);
            nouvelleEntreprise.setTypeEntreprise(TypeEntreprise.SOCIETE);
            nouvelleEntreprise.setStatutCreation(StatutCreation.VALIDEE);
            nouvelleEntreprise.setEtapeValidation(EtapeValidation.RETRAIT);
            nouvelleEntreprise.setFormeJuridique(FormeJuridique.SA);
            nouvelleEntreprise.setDomaineActivite(DomaineActivites.COMMERCE);
            nouvelleEntreprise.setDivision(koulikoroRegion);
        } else {
            // Entreprise par défaut
            nouvelleEntreprise.setReference(reference);
            nouvelleEntreprise.setNom("Entreprise par défaut");
            nouvelleEntreprise.setSigle("DEFAULT");
            nouvelleEntreprise.setAdresseDifferentIdentite(false);
            nouvelleEntreprise.setExtraitJudiciaire(true);
            nouvelleEntreprise.setAutorisationGerant(true);
            nouvelleEntreprise.setAutorisationExercice(true);
            nouvelleEntreprise.setImportExport(false);
            nouvelleEntreprise.setTypeEntreprise(TypeEntreprise.SOCIETE);
            nouvelleEntreprise.setStatutCreation(StatutCreation.EN_COURS);
            nouvelleEntreprise.setEtapeValidation(EtapeValidation.ACCUEIL);
            nouvelleEntreprise.setFormeJuridique(FormeJuridique.SARL);
            nouvelleEntreprise.setDomaineActivite(DomaineActivites.AUTRE);
            nouvelleEntreprise.setDivision(koulikoroRegion);
        }

        try {
            Entreprise savedEntreprise = entrepriseRepository.save(nouvelleEntreprise);
            System.out.println("Entreprise créée: " + savedEntreprise.getNom() + " (ID: " + savedEntreprise.getId() + ")");
            return savedEntreprise;
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'entreprise " + reference + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}