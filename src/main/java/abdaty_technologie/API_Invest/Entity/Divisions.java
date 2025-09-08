package abdaty_technologie.API_Invest.Entity;

import java.util.ArrayList;
import java.util.List;

import abdaty_technologie.API_Invest.Entity.Enum.DivisionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Divisions extends BaseEntity {

  @Column(name="code", nullable = false, unique = true)
  private String code; 
  
  @Column(name="nom", nullable = false)
  private String nom;

  @Column(name="type_division", nullable = false)
  @Enumerated(EnumType.STRING) 
  private DivisionType divisionType;

  @OneToMany(mappedBy = "division", cascade = CascadeType.ALL) 
  private List<Persons> personne = new ArrayList<>();

  @OneToMany(mappedBy = "division", cascade = CascadeType.ALL) 
  private List<Entreprise> entreprise = new ArrayList<>();
}
