package abdaty_technologie.API_Invest.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import abdaty_technologie.API_Invest.Entity.Paiement;
import abdaty_technologie.API_Invest.Entity.Enum.TypePaiements;
import abdaty_technologie.API_Invest.Entity.Enum.MobileMoney;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, String> {
    
    // Recherche par type de paiement
    List<Paiement> findByTypePaiement(TypePaiements typePaiement);
    
    // Recherche par mobile money
    List<Paiement> findByMobileMoney(MobileMoney mobileMoney);
    
    // Recherche par entreprise
    @Query("SELECT p FROM Paiement p WHERE p.entreprise.id = :entrepriseId")
    List<Paiement> findByEntrepriseId(@Param("entrepriseId") String entrepriseId);
    
    // Recherche par personne
    @Query("SELECT p FROM Paiement p WHERE p.personne.id = :personneId")
    List<Paiement> findByPersonneId(@Param("personneId") String personneId);
    
    // Recherche par montant
    List<Paiement> findByMontant(BigDecimal montant);
    
    // Recherche par montant supérieur à
    List<Paiement> findByMontantGreaterThan(BigDecimal montant);
    
    // Recherche par montant inférieur à
    List<Paiement> findByMontantLessThan(BigDecimal montant);
    
    // Recherche par plage de montant
    List<Paiement> findByMontantBetween(BigDecimal montantMin, BigDecimal montantMax);
    
    // Somme des paiements par entreprise
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.entreprise.id = :entrepriseId")
    BigDecimal sumMontantByEntrepriseId(@Param("entrepriseId") String entrepriseId);
    
    // Somme des paiements par personne
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.personne.id = :personneId")
    BigDecimal sumMontantByPersonneId(@Param("personneId") String personneId);
}
