package organizacao.finance.Guaxicash.entities.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ArchivementCompletedRow {
    public UUID uuid;             // uuid da linha em ArchivementCompleted
    public UUID archivementId;    // uuid da conquista
    public LocalDate completedAt; // data (LocalDate)

    public ArchivementCompletedRow(UUID uuid, UUID archivementId, LocalDate completedAt) {
        this.uuid = uuid;
        this.archivementId = archivementId;
        this.completedAt = completedAt;
    }
}
