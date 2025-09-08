package abdaty_technologie.API_Invest.Entity;

import java.util.ArrayList;
import java.util.List;

import abdaty_technologie.API_Invest.Entity.Enum.DomaineActivites;
import abdaty_technologie.API_Invest.Entity.Enum.EtapeValidation;
import abdaty_technologie.API_Invest.Entity.Enum.FormeJuridique;
import abdaty_technologie.API_Invest.Entity.Enum.StatutCreation;
import abdaty_technologie.API_Invest.Entity.Enum.TypeEntreprise;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Entreprise extends BaseEntity {
  @Column(name="reference", nullable = false, unique = true, length = 50)
  private String reference;

  @Column(name="nom", nullable = false, unique = true, length = 150)
  private String nom;

  @Column(name="sigle", nullable = false, unique = true, length = 15)
  private String sigle;

  @Column(name="adresse_different_identite", nullable = false)
  private Boolean adresseDifferentIdentite;

  @Column(name="extrait_judiciaire", nullable = false)
  private Boolean extraitJudiciaire;

  @Column(name="autorisation_gerant", nullable = false)
  private Boolean autorisationGerant;

  @Column(name="autorisation_exercice", nullable = false)
  private Boolean autorisationExercice;

  @Column(name="import_export", nullable = false)
  private Boolean importExport;

  @Column(name="type_entreprise", nullable = false, length = 50)
  @Enumerated(EnumType.STRING) 
  private TypeEntreprise typeEntreprise;
  
//   @Column(name="statut_societe", nullable = false, length = 50)
//   @Enumerated(EnumType.STRING) 
//   private EnumStatutSociete statutSociete;
  
  @Column(name="statut_creation", nullable = false, length = 50)
  @Enumerated(EnumType.STRING) 
  private StatutCreation statutCreation;
  
  @Column(name="etape_validation", nullable = false, length = 50)
  @Enumerated(EnumType.STRING) 
  private EtapeValidation etapeValidation;
  
  @Column(name="forme_juridique", nullable = false, length = 10)
  @Enumerated(EnumType.STRING) 
  private FormeJuridique formeJuridique;
  
  @Column(name="domaine_activite", nullable = false, length = 150)
  @Enumerated(EnumType.STRING)  
  private DomaineActivites domaineActivite;

  @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
  private List<Persons> personne = new ArrayList<>();

  @ManyToOne(optional = false)
  @JoinColumn(name = "division_id")
  private Divisions division;

  @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL) 
  private List<Documents> documents = new ArrayList<>();
  
  @OneToOne(mappedBy = "entreprise", cascade = CascadeType.ALL) 
  private Paiement paiement;
}

