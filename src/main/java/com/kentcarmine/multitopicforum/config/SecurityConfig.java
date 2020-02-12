package com.kentcarmine.multitopicforum.config;

import com.kentcarmine.multitopicforum.handlers.LoginAuthenticationSuccessHandler;
import com.kentcarmine.multitopicforum.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/**
 * Handles security and security-related configuration.
 */
@Configuration
@PropertySource("classpath:security.properties")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private LoginAuthenticationSuccessHandler loginHandler;

    @Value("${remember-me-key}")
    private String rememberMeKey;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .and()
                .userDetailsService(userDetailsService)
                .and()
                .authenticationProvider(authProvider());
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
                .antMatchers("/h2-console**").permitAll() // TODO: For debug only
                .antMatchers("/login").permitAll()
                .antMatchers("/users/*").permitAll()
                .antMatchers("/users", "/users?*", "/processSearchUsers").permitAll()
                .antMatchers("/registerUser", "/processUserRegistration", "/registrationConfirm").permitAll()
                .antMatchers("/processChangePassword", "/changePassword",
                        "/processResetPasswordStarterForm", "/resetPassword", "/resendRegistrationEmail").permitAll()
                .antMatchers("/administration").hasAnyAuthority("MODERATOR", "ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/createNewForum", "/processNewForumCreation").hasAnyAuthority("ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/forums").permitAll()
                .antMatchers("/searchTopicForums**").permitAll()
                .antMatchers("/forum/*/show/*/createPost").authenticated()
                .antMatchers("/forum/*/show/*").permitAll()
                .antMatchers("/forum/*/createThread", "/forum/*/processCreateThread").authenticated()
                .antMatchers("/forum/**").permitAll()
                .antMatchers("/processSearchThreads/*", "/searchForumThreads/**").permitAll()
                .antMatchers("/handleVoteAjax").authenticated()
                .antMatchers("/deletePostAjax", "/restorePostAjax").hasAnyAuthority("MODERATOR", "ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/promoteUserAjax, /demoteUserAjax", "/promoteUserButton/*", "/demoteUserButton/*").hasAnyAuthority("ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/lockTopicThread/*", "/unlockTopicThread/*").hasAnyAuthority("MODERATOR", "ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/manageUserDiscipline/*", "/processCreateUserDiscipline").hasAnyAuthority("MODERATOR", "ADMINISTRATOR", "SUPER_ADMINISTRATOR")
                .antMatchers("/").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/processLogin")
                .successHandler(loginHandler)
                .permitAll()
                .and()
                .logout().invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll() // Clear session on logout
                .and()
                .rememberMe().key(rememberMeKey)
                .and()
                .exceptionHandling().accessDeniedPage("/forbidden")
                .and().csrf().disable().headers().frameOptions().disable(); // TODO: For debug only
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
