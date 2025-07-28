package organizacao.finance.Guaxicash.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.service.UserService;

@Component
@RequiredArgsConstructor
public class SecurityService {

    @Autowired
    private UserService userService;

    public User obterUserLogin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String login = userDetails.getUsername();
       return (User) userService.loadUserByUsername(login);
    }
}
