package organizacao.finance.Guaxicash.resource;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.Config.TokenService;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.dto.*;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.UserService;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated AuthenticationDTO data) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.email(), data.password())
            );
            var user = (User) auth.getPrincipal();
            String access = tokenService.generateAccessToken(user);

            return ResponseEntity.ok(new LoginReponseDTO(
                    access, "Login feito com sucesso, Bem-vindo ao Guaxicash"
            ));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HttpResponseDTO("Usuário ou senha inválidos"));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HttpResponseDTO("Usuário não autenticado"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO data) {
        // Verifica se e-mail já está em uso
        if (userRepository.existsByEmail(data.email())) {
           var messagem = new HttpResponseDTO("Email Já cadastrado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messagem);
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), data.phone(), encryptedPassword, UserRole.USER);
        newUser.setRole(UserRole.USER);

        userRepository.save(newUser);
        var messagem = new HttpResponseDTO("Usuário registrado com sucesso.");
        return ResponseEntity.ok(messagem);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerByAdmin(@RequestBody @Validated RegisterDTO data) {
        if (userRepository.existsByEmail(data.email())) {
            var msg = new HttpResponseDTO("Email Já cadastrado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
        }

        var role = (data.role() == null) ? UserRole.USER : data.role(); // pode ser USER ou ADMIN
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), data.phone(), encryptedPassword, role);
        newUser.setRole(role);

        userRepository.save(newUser);
        var msg = new HttpResponseDTO("Usuário criado pelo admin com perfil " + role);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<User>> findAll() {
        List<User> list = userService.findall();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findbyId(@PathVariable UUID id) {
        User obj = userService.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value="/{id}")
    public ResponseEntity update(@PathVariable UUID id, @RequestBody @Validated User user){
        user = userService.update(id, user);
        return ResponseEntity.ok().build();
    }

}
