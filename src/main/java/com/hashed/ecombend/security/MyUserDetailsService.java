package com.hashed.ecombend.security;

import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security integration loads a User by email for authentication.
 * Called internally by AuthenticationManager during login.
 * The "username" here is actually the email address.
 */
@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email. Spring Security calls this during login to
     * retrieve the user for password comparison and enabled/locked checks.
     *
     * @param email The email address submitted in the login request
     * @return MyUserDetails wrapping the found User
     * @throws UsernameNotFoundException if no user exists with this email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No account " +
                "found with email: " + email));
        return new MyUserDetails(user);
    }
}
