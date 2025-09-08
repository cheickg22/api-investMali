package abdaty_technologie.API_Invest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import abdaty_technologie.API_Invest.Entity.Persons;
import abdaty_technologie.API_Invest.Entity.Enum.Roles;
import abdaty_technologie.API_Invest.Entity.Enum.TypePersonnes;

@Repository
public interface PersonsRepository extends JpaRepository<Persons, String> {
    
    // Recherche par email
    Optional<Persons> findByEmail(String email);
    
    // Recherche par téléphone principal
    Optional<Persons> findByTelephone1(String telephone1);
    
    // Recherche par nom et prénom
    List<Persons> findByNomAndPrenom(String nom, String prenom);
    
    // Recherche par nom
    List<Persons> findByNomContainingIgnoreCase(String nom);
    
    // Recherche par rôle
    List<Persons> findByRole(Roles role);
    
    // Recherche par type de personne
    List<Persons> findByTypePersone(TypePersonnes typePersonne);
    
    // Recherche par entreprise
    @Query("SELECT p FROM Persons p WHERE p.entreprise.id = :entrepriseId")
    List<Persons> findByEntrepriseId(@Param("entrepriseId") String entrepriseId);
    
    // Recherche par division
    @Query("SELECT p FROM Persons p WHERE p.division.id = :divisionId")
    List<Persons> findByDivisionId(@Param("divisionId") String divisionId);
    
    // Recherche des personnes autorisées
    List<Persons> findByEstAutoriser(Boolean estAutoriser);
    
    // Vérifier l'existence par email
    boolean existsByEmail(String email);
    
    // Vérifier l'existence par téléphone
    boolean existsByTelephone1(String telephone1);
    
    boolean existsByTelephone2(String telephone2);
}
