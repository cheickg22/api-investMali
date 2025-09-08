package abdaty_technologie.API_Invest.Entity;

import java.math.BigDecimal;

import abdaty_technologie.API_Invest.Entity.Enum.MobileMoney;
import abdaty_technologie.API_Invest.Entity.Enum.TypePaiements;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Paiement extends BaseEntity {

    @Column(name="type_paiement", nullable = false)
    @Enumerated(EnumType.STRING) 
    private TypePaiements typePaiement;
    
    @Column(name="mobile_money", nullable = true)
    @Enumerated(EnumType.STRING) 
    private MobileMoney mobileMoney; // nullable si non MOBILE_MONEY
    
    @Column(name = "montant", nullable = false, precision=18, scale=2) 
    private BigDecimal montant;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personne_id")
    private Persons personne;

    //GETTER AND SETTER
    
}

