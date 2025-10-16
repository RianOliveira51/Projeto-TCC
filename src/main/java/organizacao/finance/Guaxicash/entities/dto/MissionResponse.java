package organizacao.finance.Guaxicash.entities.dto;

import java.time.LocalDate;
import java.util.UUID;

public class MissionResponse {
    public UUID uuid;
    public String title;
    public String description;
    public int value;
    public boolean completed;
    public LocalDate completedAt;

    public MissionResponse(UUID uuid, String title, String description, int value,
                           boolean completed, LocalDate completedAt) {
        this.uuid = uuid;
        this.title = title;
        this.description = description;
        this.value = value;
        this.completed = completed;
        this.completedAt = completedAt;
    }
}
