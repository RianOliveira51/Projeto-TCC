package organizacao.finance.Guaxicash.Config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    //gerar token
    public String generateToken(User user) {
        try{
            //algortimo de geração de token
            Algorithm algorithm =  Algorithm.HMAC256(secret);
            //criando token
            String token = JWT.create()
                    //nome da aplicação
                    .withIssuer("auth-api")
                    //usuario
                    .withSubject(user.getEmail())
                    //tempo de expiração
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;
        }catch (JWTCreationException e){
            throw new RuntimeException("Error white generating token", e);
        }
    }

    //validar token

    public String validateToken(String token) {
        try {
            Algorithm algorithm =  Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch(JWTVerificationException e){
            throw new RuntimeException("");
        }
    }
    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(10).toInstant(ZoneOffset.of("-03:00"));
    }
}
