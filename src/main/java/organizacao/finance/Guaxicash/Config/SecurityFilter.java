package organizacao.finance.Guaxicash.Config;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.ArchivementService;

import java.io.IOException;
import java.time.YearMonth;

// securityFilter.java (exemplo — adapte aos seus nomes)
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired private TokenService tokenService;
    @Autowired private UserRepository userRepository;
    @Autowired private ArchivementService archivementService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = recoverToken(req);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String subject = tokenService.validateAccessAndGetSubject(token);
                User user = (User) userRepository.findByEmail(subject); // seu UserDetails é User
                if (user != null) {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // 👇 Hook: primeiro login (ou toda auth) — é idempotente
                    archivementService.evaluateAllFor(user, YearMonth.now());
                }
            } catch (Exception e) {
                // trate expiração/invalid token como já fazia
            }
        }

        chain.doFilter(req, res);
    }

    private String recoverToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
