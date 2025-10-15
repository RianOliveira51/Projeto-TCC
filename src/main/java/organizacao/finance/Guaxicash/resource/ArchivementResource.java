package organizacao.finance.Guaxicash.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Archivement;
import organizacao.finance.Guaxicash.entities.dto.ArchivementResponse;
import organizacao.finance.Guaxicash.entities.dto.CompleteArchivementResponse;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.ArchivementService;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/archivement")
public class ArchivementResource {

    @Autowired
    private ArchivementService archivementService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> createBank(@RequestBody Archivement archivement) {
        Archivement created = archivementService.insert(archivement);
        URI location = URI.create("/archivement/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Conquista criada com sucesso."));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Archivement>> findAll() {
        List<Archivement> list = archivementService.findAll();
        return ResponseEntity.ok(list);
    }

    // === NOVOS ===

    @GetMapping("/arch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchivementResponse>> listAllWithStatus() {
        return ResponseEntity.ok(archivementService.listAllWithUserStatus());
    }

    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ArchivementResponse>> listCompleted() {
        return ResponseEntity.ok(archivementService.listCompleted());
    }

    @GetMapping("/trophies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> trophiesCount() {
        return ResponseEntity.ok(archivementService.trophiesCount());
    }

    @PostMapping("/complete/{archivementId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompleteArchivementResponse> complete(@PathVariable UUID archivementId) {
        return ResponseEntity.ok(archivementService.complete(archivementId));
    }

    @PostMapping("/evaluate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> evaluate(@RequestParam(required = false) String ym) {
        YearMonth ref = (ym != null) ? YearMonth.parse(ym) : YearMonth.now();
        archivementService.evaluateAllForMe(ref);
        return ResponseEntity.ok().build();
    }
}
