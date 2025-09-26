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
import organizacao.finance.Guaxicash.repositories.UserRepository;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = recoverToken(req);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String subject = tokenService.validateAccessAndGetSubject(token); // lança se inválido/expirado
                UserDetails user = userRepository.findByEmail(subject);
                if (user == null) {
                    write401(res,
                            "invalid_token",
                            "Usuário do token não existe mais.");
                    return;
                }

                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (TokenExpiredException e) {
                write401(res,
                        "invalid_token",
                        "Sessão expirada. Faça login novamente.");
                return;

            } catch (JWTVerificationException e) {
                write401(res,
                        "invalid_token",
                        "Token inválido");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private String recoverToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return auth.substring(7);
    }

    private void write401(HttpServletResponse res, String error, String description) throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        // Header padrão do OAuth 2.0 / RFC 6750
        res.setHeader("WWW-Authenticate",
                "Bearer error=\"" + error + "\", error_description=\"" + description.replace("\"","'") + "\"");
        res.getWriter().write("{\"message\":\"" + description + "\"}");
    }
}
