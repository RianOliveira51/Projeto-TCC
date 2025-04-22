package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.resource.dto.AuthenticationDTO;
import organizacao.finance.Guaxicash.resource.dto.RegisterDTO;
import organizacao.finance.Guaxicash.service.UserService;

import java.util.List;

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
    private UserService userservice;

    @Autowired
    private UserRepository userrepository;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Validated AuthenticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        return ResponseEntity.ok().build();
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

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        List<User> list = userService.findall();
        return ResponseEntity.ok().body(list);
    }

}
