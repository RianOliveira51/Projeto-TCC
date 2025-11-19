package organizacao.finance.Guaxicash.entities.dto;

import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.Rank;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;

import java.util.UUID;

public record UserMeDTO(
        UUID uuid,
        String name,
        String email,
        String phone,
        int xp,
        Rank rank,
        UserRole role,
        Active active
) {}
