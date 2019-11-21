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

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + username);
        }

        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        return  new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), user.isEnabled(), accountNonExpired, credentialsNonExpired,
                accountNonLocked, getAuthorities(user.getAuthorities())
        );

    }

    private static List<GrantedAuthority> getAuthorities(Set<Authority> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Authority role: roles) {
            authorities.add(new SimpleGrantedAuthority(role.getAuthority().toString()));
        }

        return authorities;
    }
}
