package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.model.Authority;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of UserDetailsService.
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private MessageService messageService;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, MessageService messageService) {
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
//        System.out.println("### in loadUserByUsername. Username = " + username);
        if (user == null) {
            String msg = messageService.getMessage("Exception.user.notfound", username);
            throw new UsernameNotFoundException(msg);
        }

//        System.out.println("### in loadUserByUsername. authorities = " + user.getAuthorities().toString());

        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        return  new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), user.isEnabled(), accountNonExpired, credentialsNonExpired,
                accountNonLocked, getAuthorities(user.getAuthorities())
        );
    }

    /**
     * Converts a Set of Authorities into a List of GrantedAuthorites
     *
     * @param roles Set of Authorities to convert
     * @return the equivalent List of GratnedAuthorities
     */
    private static List<GrantedAuthority> getAuthorities(Set<Authority> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Authority role: roles) {
            authorities.add(new SimpleGrantedAuthority(role.getAuthority().toString()));
        }

        return authorities;
    }
}
