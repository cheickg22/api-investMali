package abdaty_technologie.API_Invest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import abdaty_technologie.API_Invest.Entity.Utilisateurs;

@Repository
public interface UtilisateursRepository extends JpaRepository<Utilisateurs, String> {
    
    // Recherche par nom d'utilisateur
    Optional<Utilisateurs> findByUtilisateur(String utilisateur);
    
    // Recherche par personne associée
    @Query("SELECT u FROM Utilisateurs u WHERE u.personne.id = :personneId")
    Optional<Utilisateurs> findByPersonneId(@Param("personneId") String personneId);
    
    // Vérifier l'existence par nom d'utilisateur
    boolean existsByUtilisateur(String utilisateur);
    
    // Vérifier l'existence par personne
    @Query("SELECT COUNT(u) > 0 FROM Utilisateurs u WHERE u.personne.id = :personneId")
    boolean existsByPersonneId(@Param("personneId") String personneId);
}
