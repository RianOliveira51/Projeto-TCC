package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.Config.TokenService;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.Rank;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.dto.*;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.UserService;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private TokenService tokenService;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // ===== AUTH =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated AuthenticationDTO data) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.email(), data.password())
            );
            var user = (User) auth.getPrincipal();
            String access = tokenService.generateAccessToken(user);

            return ResponseEntity.ok(new LoginReponseDTO(
                    access, "Login feito com sucesso, Bem-vindo ao Guaxicash"
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HttpResponseDTO("Usuário ou senha inválidos"));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HttpResponseDTO("Usuário não autenticado"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO data) {
        if (userRepository.existsByEmail(data.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HttpResponseDTO("Email Já cadastrado"));
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), data.phone(), encryptedPassword, UserRole.USER, Active.ACTIVE, 0, Rank.FERRO);
        newUser.setRole(UserRole.USER);
        userRepository.save(newUser);
        return ResponseEntity.ok(new HttpResponseDTO("Usuário registrado com sucesso."));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerByAdmin(@RequestBody @Validated RegisterDTO data) {
        if (userRepository.existsByEmail(data.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HttpResponseDTO("Email Já cadastrado"));
        }
        var role = (data.role() == null) ? UserRole.USER : data.role();
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), data.phone(), encryptedPassword, role, Active.ACTIVE, 0, Rank.FERRO);
        newUser.setRole(role);
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new HttpResponseDTO("Usuário criado pelo admin com perfil " + role));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<User>> findAll(@RequestParam(name = "active", required = false) Active active) {
        List<User> list = (active == null) ? userService.findall() : userService.findAllByActive(active);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findbyId(@PathVariable UUID id) {
        User obj = userService.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserMeDTO> getMe() {
        UserMeDTO dto = userService.getAuthenticatedUser();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@AuthenticationPrincipal User me, @RequestBody @Validated UpdateUserDTO data) {
        try {
            User patch = new User();
            patch.setName(data.name());
            patch.setEmail(data.email());
            patch.setPhone(data.phone());
            patch.setPassword(data.password());
            userService.update(me.getUuid(), patch);
            return ResponseEntity.ok(new HttpResponseDTO("Usuário atualizado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HttpResponseDTO("e-mail já cadastrado"));
        }
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> softDelete(@PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.ok(new HttpResponseDTO("Usuário desativado."));
    }

    @PutMapping("/enable/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enable(@PathVariable UUID id) {
        userService.enable(id);
        return ResponseEntity.ok(new HttpResponseDTO("Usuário reativado."));
    }
}
