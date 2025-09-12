package abdaty_technologie.API_Invest.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import abdaty_technologie.API_Invest.Entity.Entreprise;
import abdaty_technologie.API_Invest.Entity.Paiement;
import abdaty_technologie.API_Invest.Entity.Persons;
import abdaty_technologie.API_Invest.Entity.Enum.StatutPaiement;
import abdaty_technologie.API_Invest.constants.Messages;
import abdaty_technologie.API_Invest.dto.requests.PaiementRequest;
import abdaty_technologie.API_Invest.dto.responses.PaiementResponse;
import abdaty_technologie.API_Invest.exception.NotFoundException;
import abdaty_technologie.API_Invest.repository.EntrepriseRepository;
import abdaty_technologie.API_Invest.repository.PaiementRepository;
import abdaty_technologie.API_Invest.repository.PersonsRepository;
import abdaty_technologie.API_Invest.service.IPaiementService;

@Service
@Transactional
public class PaiementServiceImpl implements IPaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PersonsRepository personsRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Override
    public PaiementResponse creerPaiement(PaiementRequest request) {
        // Vérifier que la personne existe
        Persons personne = personsRepository.findById(request.getPersonneId())
                .orElseThrow(() -> new NotFoundException(Messages.PERSON_NOT_FOUND));

        Entreprise entreprise = null;
        if (request.getEntrepriseId() != null) {
            entreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new NotFoundException(Messages.ENTERPRISE_NOT_FOUND));
        }

        // Créer le paiement
        Paiement paiement = new Paiement();
        paiement.setTypePaiement(request.getTypePaiement());
        paiement.setMontant(request.getMontant());
        paiement.setPersonne(personne);
        paiement.setEntreprise(entreprise);
        paiement.setDescription(request.getDescription());
        paiement.setNumeroTelephone(request.getNumeroTelephone());
        paiement.setNumeroCompte(request.getNumeroCompte());
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setReferenceTransaction(genererReferenceTransaction());

        // Sauvegarder
        Paiement paiementSauve = paiementRepository.save(paiement);

        return convertirEnResponse(paiementSauve);
    }

    @Override
    public List<PaiementResponse> obtenirTousPaiements() {
        return paiementRepository.findAll().stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementResponse obtenirPaiementParId(String id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Messages.PAYMENT_NOT_FOUND + id));
        return convertirEnResponse(paiement);
    }

    @Override
    public PaiementResponse obtenirPaiementParReference(String reference) {
        Paiement paiement = paiementRepository.findByReferenceTransaction(reference)
                .orElseThrow(() -> new NotFoundException(Messages.PAYMENT_NOT_FOUND_BY_REFERENCE + reference));
        return convertirEnResponse(paiement);
    }

    @Override
    public List<PaiementResponse> obtenirPaiementsParPersonne(String personneId) {
        return paiementRepository.findByPersonneId(personneId).stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaiementResponse> obtenirPaiementsParEntreprise(String entrepriseId) {
        return paiementRepository.findByEntrepriseId(entrepriseId).stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaiementResponse> obtenirPaiementsParStatut(StatutPaiement statut) {
        return paiementRepository.findByStatut(statut).stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementResponse validerPaiement(String id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Messages.PAYMENT_NOT_FOUND + id));
        
        paiement.setStatut(StatutPaiement.VALIDE);
        paiement.setDatePaiement(LocalDateTime.now());
        
        Paiement paiementMisAJour = paiementRepository.save(paiement);
        return convertirEnResponse(paiementMisAJour);
    }

    @Override
    public PaiementResponse refuserPaiement(String id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Messages.PAYMENT_NOT_FOUND + id));
        
        paiement.setStatut(StatutPaiement.REFUSE);
        
        Paiement paiementMisAJour = paiementRepository.save(paiement);
        return convertirEnResponse(paiementMisAJour);
    }

    @Override
    public PaiementResponse annulerPaiement(String id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Messages.PAYMENT_NOT_FOUND + id));
        
        paiement.setStatut(StatutPaiement.ANNULE);
        
        Paiement paiementMisAJour = paiementRepository.save(paiement);
        return convertirEnResponse(paiementMisAJour);
    }

    @Override
    public BigDecimal calculerTotalPaiementsPersonne(String personneId, StatutPaiement statut) {
        BigDecimal total = paiementRepository.sumMontantByPersonneIdAndStatut(personneId, statut);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculerTotalPaiementsEntreprise(String entrepriseId, StatutPaiement statut) {
        BigDecimal total = paiementRepository.sumMontantByEntrepriseIdAndStatut(entrepriseId, statut);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public void supprimerPaiement(String id) {
        if (!paiementRepository.existsById(id)) {
            throw new NotFoundException(Messages.PAYMENT_NOT_FOUND + id);
        }
        paiementRepository.deleteById(id);
    }

    private String genererReferenceTransaction() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaiementResponse convertirEnResponse(Paiement paiement) {
        PaiementResponse response = new PaiementResponse();
        response.setId(paiement.getId());
        response.setTypePaiement(paiement.getTypePaiement());
        response.setStatut(paiement.getStatut());
        response.setMontant(paiement.getMontant());
        response.setReferenceTransaction(paiement.getReferenceTransaction());
        response.setDescription(paiement.getDescription());
        response.setDatePaiement(paiement.getDatePaiement());
        response.setDateCreation(paiement.getCreation() != null ? paiement.getCreation().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        response.setNumeroTelephone(paiement.getNumeroTelephone());
        response.setNumeroCompte(paiement.getNumeroCompte());

        // Informations de la personne
        if (paiement.getPersonne() != null) {
            response.setPersonneId(paiement.getPersonne().getId());
            response.setPersonneNom(paiement.getPersonne().getNom());
            response.setPersonnePrenom(paiement.getPersonne().getPrenom());
        }

        // Informations de l'entreprise
        if (paiement.getEntreprise() != null) {
            response.setEntrepriseId(paiement.getEntreprise().getId());
            response.setEntrepriseNom(paiement.getEntreprise().getNom());
        }

        return response;
    }
}
