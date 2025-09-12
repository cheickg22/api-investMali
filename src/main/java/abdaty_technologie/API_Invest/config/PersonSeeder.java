package abdaty_technologie.API_Invest.config;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import abdaty_technologie.API_Invest.Entity.Persons;
import abdaty_technologie.API_Invest.Entity.Enum.AntenneAgents;
import abdaty_technologie.API_Invest.Entity.Enum.Civilites;
import abdaty_technologie.API_Invest.Entity.Enum.Nationalites;
import abdaty_technologie.API_Invest.Entity.Enum.Roles;
import abdaty_technologie.API_Invest.Entity.Enum.Sexes;
import abdaty_technologie.API_Invest.Entity.Enum.SituationMatrimoniales;
import abdaty_technologie.API_Invest.repository.PersonsRepository;

@Component
@Profile({"default","dev"})
public class PersonSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PersonSeeder.class);

    private final PersonsRepository personsRepository;

    public PersonSeeder(PersonsRepository personsRepository) {
        this.personsRepository = personsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Ne pas reseeder si un SUPER_ADMIN existe déjà
        boolean superAdminExists = personsRepository.findByRole(Roles.SUPER_ADMIN).stream().findAny().isPresent();
        if (superAdminExists) {
            log.info("[PersonSeeder] SUPER_ADMIN existe déjà, pas de seed.");
            return;
        }

        // Création d'un SUPER_ADMIN par défaut (compte technique)
        Date dob1985 = new GregorianCalendar(1985, Calendar.JANUARY, 1).getTime();

        Persons admin = new Persons();
        admin.setNom("Admin");
        admin.setPrenom("Super");
        admin.setEmail("admin@example.com");
        // Téléphone au format E.164 (indicatif pays + numéro)
        admin.setTelephone1("+22370000000");
        admin.setTelephone2(null);
        admin.setDateNaissance(dob1985);
        admin.setLieuNaissance("Bamako");
        admin.setEstAutoriser(true); // majeur
        admin.setNationnalite(Nationalites.MALIENNE);
        admin.setEntrepriseRole(null); // non requis pour SUPER_ADMIN
        admin.setAntenneAgent(AntenneAgents.BAMAKO); // valeur par défaut
        admin.setSexe(Sexes.MASCULIN);
        admin.setSituationMatrimoniale(SituationMatrimoniales.CELIBATAIRE);
        admin.setCivilite(Civilites.MONSIEUR);
        admin.setRole(Roles.SUPER_ADMIN);
        admin.setCreation(Instant.now());
        admin.setModification(Instant.now());

        personsRepository.save(admin);
        log.info("[PersonSeeder] SUPER_ADMIN seedé: email={} id={}", admin.getEmail(), admin.getId());
    }
}
