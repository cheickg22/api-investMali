package abdaty_technologie.API_Invest.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import abdaty_technologie.API_Invest.Entity.Enum.StatutPaiement;
import abdaty_technologie.API_Invest.Entity.Enum.TypePaiement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Paiement extends BaseEntity {

    @Column(name="type_paiement", nullable = false)
    @Enumerated(EnumType.STRING) 
    private TypePaiement typePaiement;
    
    @Column(name="statut", nullable = false)
    @Enumerated(EnumType.STRING) 
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;
    
    @Column(name = "montant", nullable = false, precision=18, scale=2) 
    private BigDecimal montant;

    @Column(name = "reference_transaction", unique = true)
    private String referenceTransaction;

    @Column(name = "description")
    private String description;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "numero_telephone")
    private String numeroTelephone; // Pour mobile money

    @Column(name = "numero_compte")
    private String numeroCompte; // Pour virements bancaires

    @ManyToOne(optional = true)
    @JoinColumn(name = "entreprise_id", nullable = true)
    private Entreprise entreprise;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personne_id")
    private Persons personne;
}

