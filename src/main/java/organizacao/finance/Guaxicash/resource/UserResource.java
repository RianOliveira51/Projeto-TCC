package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;
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


    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        List<User> list = userService.findall();
        return ResponseEntity.ok().body(list);
    }

   /* @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        var user = userService.findByUsername(loginRequest.username());
        if (user == null || !user.isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("User or password is invalid");
        }
    }*/

}
