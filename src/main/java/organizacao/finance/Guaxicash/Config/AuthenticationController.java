package organizacao.finance.Guaxicash.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.resource.dto.AuthenticationDTO;
import organizacao.finance.Guaxicash.resource.dto.LoginReponseDTO;
import organizacao.finance.Guaxicash.resource.dto.RegisterDTO;
import organizacao.finance.Guaxicash.service.UserService;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userservice;

    @Autowired
    private UserRepository userrepository;

    @Autowired
    private TokenService tokenService;


    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Validated AuthenticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User)auth.getPrincipal());

        return ResponseEntity.ok(new LoginReponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register (@RequestBody @Validated RegisterDTO data){
        if(this.userservice.loadUserByUsername(data.email()) != null) return ResponseEntity.badRequest().build();
        //Pegando hash da senha do usuario
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(null, data.name(), data.email(), data.phone(), encryptedPassword, data.role() );

        this.userrepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
