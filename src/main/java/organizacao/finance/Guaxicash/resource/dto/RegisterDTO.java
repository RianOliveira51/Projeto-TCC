package organizacao.finance.Guaxicash.resource.dto;

import organizacao.finance.Guaxicash.entities.UserRole;

public record RegisterDTO(String name, String email, String phone, String password, UserRole role ){
}
