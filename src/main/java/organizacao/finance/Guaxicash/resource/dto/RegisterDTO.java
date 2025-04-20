package organizacao.finance.Guaxicash.resource.dto;

import organizacao.finance.Guaxicash.entities.UserRole;

public record RegisterDTO(String name, String username, String phone, String password, UserRole role ){
}
