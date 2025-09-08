package abdaty_technologie.API_Invest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import abdaty_technologie.API_Invest.Entity.Entreprise;
import abdaty_technologie.API_Invest.Entity.Enum.StatutCreation;
import abdaty_technologie.API_Invest.Entity.Enum.TypeEntreprise;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, String> {
    
    // Recherche par référence
    Optional<Entreprise> findByReference(String reference);
    
    // Recherche par nom
    Optional<Entreprise> findByNom(String nom);
    
    // Recherche par sigle
    Optional<Entreprise> findBySigle(String sigle);
    
    // Recherche par type d'entreprise
    List<Entreprise> findByTypeEntreprise(TypeEntreprise typeEntreprise);
    
    // Recherche par statut de création
    List<Entreprise> findByStatutCreation(StatutCreation statutCreation);
    
    // Recherche par division
    @Query("SELECT e FROM Entreprise e WHERE e.division.id = :divisionId")
    List<Entreprise> findByDivisionId(@Param("divisionId") String divisionId);
    
    // Vérifier l'existence par référence
    boolean existsByReference(String reference);
    
    // Vérifier l'existence par nom
    boolean existsByNom(String nom);
    
    // Vérifier l'existence par sigle
    boolean existsBySigle(String sigle);
}
