package organizacao.finance.Guaxicash.entities.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CompleteArchivementResponse {
    public UUID archivementId;
    public boolean alreadyCompleted;
    public LocalDate completedAt;
    public long trophiesTotal;

    public CompleteArchivementResponse(UUID archivementId, boolean alreadyCompleted, LocalDate completedAt, long trophiesTotal) {
        this.archivementId = archivementId;
        this.alreadyCompleted = alreadyCompleted;
        this.completedAt = completedAt;
        this.trophiesTotal = trophiesTotal;
    }
}
