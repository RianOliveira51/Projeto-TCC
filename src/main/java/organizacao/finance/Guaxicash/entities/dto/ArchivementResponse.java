package organizacao.finance.Guaxicash.entities.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ArchivementResponse {
    public UUID uuid;
    public String title;
    public String description;
    public boolean completed;
    public LocalDate completedAt;

    public ArchivementResponse(UUID uuid, String title, String description, boolean completed, LocalDate completedAt) {
        this.uuid = uuid;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.completedAt = completedAt;
    }
}
