package abdaty_technologie.API_Invest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import abdaty_technologie.API_Invest.Entity.Divisions;
import abdaty_technologie.API_Invest.Entity.Enum.DivisionType;

@Repository
public interface DivisionsRepository extends JpaRepository<Divisions, String> {
    
    // Recherche par code
    Optional<Divisions> findByCode(String code);
    
    // Recherche par nom
    List<Divisions> findByNomContainingIgnoreCase(String nom);
    
    // Recherche par type de division
    List<Divisions> findByDivisionType(DivisionType divisionType);
    
    // Vérifier l'existence par code
    boolean existsByCode(String code);
}
