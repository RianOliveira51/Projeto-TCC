package organizacao.finance.Guaxicash.Config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.issuer}")
    private String issuer;

    @Value("${api.security.token.access-ttl-minutes}")
    private long accessTtlMinutes;

    private Algorithm alg() {
        return Algorithm.HMAC256(secret);
    }

    public String generateAccessToken(User user) {
        return buildToken(
                user.getEmail(),
                "access",
                Instant.now().plus(accessTtlMinutes, ChronoUnit.MINUTES),
                user.getName()
        );
    }

    public String generateAccessToken(String subject) {
        return buildToken(
                subject,
                "access",
                Instant.now().plus(accessTtlMinutes, ChronoUnit.MINUTES),
                null
        );
    }

    public String validateAccessAndGetSubject(String accessToken) {
        var decoded = verify(accessToken);
        var typ = decoded.getClaim("typ").asString();
        if (!"access".equals(typ)) throw new JWTVerificationException("Token não é de acesso");
        return decoded.getSubject();
    }

    private com.auth0.jwt.interfaces.DecodedJWT verify(String token) {
        return JWT.require(alg())
                .withIssuer(issuer)
                .build()
                .verify(token);
    }

    private String buildToken(String subject, String typ, Instant exp, String name) {
        try {
            var builder = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(subject)
                    .withClaim("typ", typ)
                    .withIssuedAt(new Date())
                    .withExpiresAt(Date.from(exp));

            if (name != null) builder.withClaim("name", name);

            return builder.sign(alg());
        } catch (JWTCreationException e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }
}
