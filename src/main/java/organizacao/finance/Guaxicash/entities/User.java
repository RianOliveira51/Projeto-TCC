package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role = UserRole.USER;

    public User() {}

    public User(UUID uuid, String name, String email, String phone, String password, UserRole role) {
            this.uuid = uuid;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.role = role;
        }

        public UUID getUuid () {
            return uuid;
        }

        public void setUuid (UUID uuid){
            this.uuid = uuid;
        }

        public String getName () {
            return name;
        }

        public void setName (String name){
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public UserRole getRole () {
            return role;
        }

        public void setRole (UserRole role){
            this.role = role;
        }

        @Override
        public boolean isAccountNonExpired () {
            return true;
        }

        @Override
        public boolean isAccountNonLocked () {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired () {
            return true;
        }

        @Override
        public boolean isEnabled () {
            return true;
        }

        public void setEmail(String email){
            this.email = email;
        }

        public String getPhone () {
            return phone;
        }

        public void setPhone (String phone){
            this.phone = phone;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities () {
            //se for admin tera todas as permissoes
            if (this.role == UserRole.ADMIN)
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
                //se for usuario, só terá permissão de usuario.
            else return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        public String getPassword () {
            return password;
        }

    @Override
    public String getUsername() {
        return this.email;
    }

    public void setPassword (String password){
            this.password = password;
        }


        @Override
        public String toString () {
            return "Users{" +
                    "uuid=" + uuid +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }

    }


