package organizacao.finance.Guaxicash.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.Rank;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;

@Configuration
public class AdminBootstrapConfig {
    @Bean
    public CommandLineRunner ensureTwoAdmins(UserRepository userRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            final String email1 = "admin@gmail.com";
            final String email2 = "admin2@gmail.com";
            final String rawPwd  = "Admin456123";

            try {
                long activeAdmins = userRepository.countByRoleAndActive(UserRole.ADMIN, Active.ACTIVE);
                if (activeAdmins >= 2) {
                    System.out.println("[Guaxicash] Já existem ≥2 ADMINs ativos. Nenhuma ação.");
                    return;
                }

                // Garante o primeiro ADMIN (admin@gmail.com)
                if (activeAdmins == 0) {
                    ensureAdmin(userRepository, passwordEncoder, email1, rawPwd);
                }

                // Reconta (alguém pode já ser ADMIN ativo com outro email)
                activeAdmins = userRepository.countByRoleAndActive(UserRole.ADMIN, Active.ACTIVE);

                // Se ainda só houver 1 ADMIN ativo, garanta o segundo (admin2@gmail.com),
                // OU, se o admin1 ainda não é admin@gmail.com, garanta admin@gmail.com.
                if (activeAdmins < 2) {
                    // Se admin@gmail.com já é ADMIN ativo, cria/promove admin2@gmail.com.
                    var ud1 = userRepository.findByEmail(email1);
                    if (ud1 instanceof User u1 && u1.getRole() == UserRole.ADMIN && u1.getActive() == Active.ACTIVE) {
                        ensureAdmin(userRepository, passwordEncoder, email2, rawPwd);
                    } else {
                        // Se ainda não temos admin@gmail.com como ADMIN ativo, garanta-o como o "segundo".
                        ensureAdmin(userRepository, passwordEncoder, email1, rawPwd);
                    }
                }

                System.out.println("[Guaxicash] Seeder de ADMIN finalizado. Lembre-se de alterar as senhas padrão!");

            } catch (Exception e) {
                System.err.println("[Guaxicash] Falha no seeder de ADMIN: " + e.getMessage());
            }
        };
    }

    private void ensureAdmin(UserRepository repo, PasswordEncoder enc, String email, String rawPwd) {
        var ud = repo.findByEmail(email);

        if (ud instanceof User u) {
            // promove/ajusta
            u.setRole(UserRole.ADMIN);
            u.setActive(Active.ACTIVE);
            u.setPassword(enc.encode(rawPwd));
            if (u.getName() == null || u.getName().isBlank()) u.setName("Administrator");
            if (u.getRank() == null) u.setRank(Rank.FERRO);
            //try { if (u.getXp() == null) u.setXp(0); } catch (Exception ignore) {}
            repo.save(u);
            System.out.println("[Guaxicash] ADMIN ajustado/promovido: " + email);
        } else {
            // cria
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPhone(null);
            admin.setPassword(enc.encode(rawPwd));
            admin.setRole(UserRole.ADMIN);
            admin.setActive(Active.ACTIVE);
            admin.setRank(Rank.FERRO);
            //try { admin.setXp(0); } catch (Exception ignore) {}
            repo.save(admin);
            System.out.println("[Guaxicash] ADMIN criado: " + email);
        }
    }
}
