package abdaty_technologie.API_Invest.controller;

import abdaty_technologie.API_Invest.dto.request.PersonCreateRequest;
import abdaty_technologie.API_Invest.dto.request.PersonUpdateRequest;
import abdaty_technologie.API_Invest.dto.response.PersonResponse;
import abdaty_technologie.API_Invest.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
public class PersonController {

    @Autowired
    private PersonService personService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest req) {
        return ResponseEntity.ok(personService.create(req));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(personService.getById(id));
        
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PersonResponse>> list() {
        return ResponseEntity.ok(personService.list());
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonResponse> update(@PathVariable String id, @Valid @RequestBody PersonUpdateRequest req) {
        return ResponseEntity.ok(personService.update(id, req));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
