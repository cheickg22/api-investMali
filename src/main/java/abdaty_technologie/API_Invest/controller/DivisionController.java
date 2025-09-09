package abdaty_technologie.API_Invest.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import abdaty_technologie.API_Invest.Entity.Divisions;
import abdaty_technologie.API_Invest.Entity.Enum.DivisionType;
import abdaty_technologie.API_Invest.repository.DivisionsRepository;
import abdaty_technologie.API_Invest.service.IDivisionImportService;

@RestController
@RequestMapping("/api/divisions")
public class DivisionController {

    @Autowired
    private DivisionsRepository divisionsRepository;

    @Autowired
    private IDivisionImportService divisionImportService;

    /**
     * Import des divisions depuis un fichier Excel
     */
    @PostMapping("/import")
    public ResponseEntity<String> importDivisions(@RequestParam("file") MultipartFile file) {
        try {
            String result = divisionImportService.importDivisionsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du fichier: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne: " + e.getMessage());
        }
    }

    /**
     * Télécharger le template Excel pour l'import
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = divisionImportService.generateExcelTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "template_divisions.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer toutes les régions
     */
    @GetMapping("/regions")
    public ResponseEntity<List<Divisions>> getAllRegions() {
        try {
            List<Divisions> regions = divisionsRepository.findByDivisionType(DivisionType.REGION);
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Récupérer les cercles d'une région
     */
    @GetMapping("/regions/{regionId}/cercles")
    public ResponseEntity<List<Divisions>> getCerclesByRegion(@PathVariable String regionId) {
        List<Divisions> cercles = divisionsRepository.findCerclesByRegionId(regionId);
        return ResponseEntity.ok(cercles);
    }

    /**
     * Récupérer les arrondissements d'un cercle
     */
    @GetMapping("/cercles/{cercleId}/arrondissements")
    public ResponseEntity<List<Divisions>> getArrondissementsByCercle(@PathVariable String cercleId) {
        List<Divisions> arrondissements = divisionsRepository.findArrondissementsByCercleId(cercleId);
        return ResponseEntity.ok(arrondissements);
    }

    /**
     * Récupérer les communes d'un arrondissement
     */
    @GetMapping("/arrondissements/{arrondissementId}/communes")
    public ResponseEntity<List<Divisions>> getCommunesByArrondissement(@PathVariable String arrondissementId) {
        List<Divisions> communes = divisionsRepository.findCommunesByArrondissementId(arrondissementId);
        return ResponseEntity.ok(communes);
    }

    /**
     * Récupérer les quartiers d'une commune
     */
    @GetMapping("/communes/{communeId}/quartiers")
    public ResponseEntity<List<Divisions>> getQuartiersByCommune(@PathVariable String communeId) {
        List<Divisions> quartiers = divisionsRepository.findQuartiersByCommuneId(communeId);
        return ResponseEntity.ok(quartiers);
    }

    /**
     * Compter le nombre total de divisions
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDivisions() {
        long count = divisionsRepository.count();
        return ResponseEntity.ok(count);
    }

    /**
     * Récupérer toutes les divisions (limité à 10 pour éviter les timeouts)
     */
    @GetMapping
    public ResponseEntity<List<Divisions>> getAllDivisions() {
        List<Divisions> divisions = divisionsRepository.findAll().stream().limit(10).toList();
        return ResponseEntity.ok(divisions);
    }

    /**
     * Récupérer une division par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Divisions> getDivisionById(@PathVariable String id) {
        return divisionsRepository.findById(id)
                .map(division -> ResponseEntity.ok(division))
                .orElse(ResponseEntity.notFound().build());
    }
}
