package organizacao.finance.Guaxicash.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.repositories.MissionsRepository;
import organizacao.finance.Guaxicash.service.MissionsService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/missions")
public class MissionsResource {

    private final MissionsService missionsService;
    private MissionsRepository missionsRepository;

    public MissionsResource(MissionsService missionsService) {
        this.missionsService = missionsService;
    }

    @GetMapping
    public ResponseEntity<List<Missions>> listForCurrentUser() {
        var list = missionsService.listForCurrentUser();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Missions>> listAll() {
        return ResponseEntity.ok(missionsService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Missions> get(@PathVariable UUID id) {
        return ResponseEntity.ok(missionsService.get(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Missions> create(@RequestBody Missions body) {
        Missions created = missionsService.create(body);
        return ResponseEntity.created(URI.create("/missions/" + created.getUuid())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Missions> update(@PathVariable UUID id, @RequestBody Missions body) {
        return ResponseEntity.ok(missionsService.update(id, body));
    }

    // excluir (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        missionsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
