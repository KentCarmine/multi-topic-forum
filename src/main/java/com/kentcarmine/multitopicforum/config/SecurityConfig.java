package com.kentcarmine.multitopicforum.config;

import com.kentcarmine.multitopicforum.helpers.AlreadyLoggedInAccessDeniedHandler;
import com.kentcarmine.multitopicforum.helpers.AlreadyLoggedInFailureHandler;
import com.kentcarmine.multitopicforum.helpers.LoginAuthenticationSuccessHandler;
import com.kentcarmine.multitopicforum.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private LoginAuthenticationSuccessHandler loginHandler;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .and()
                .userDetailsService(userDetailsService)
                .and()
                .authenticationProvider(authProvider());
    }

//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.authorizeRequests()
//                .antMatchers("/h2-console**").permitAll() // TODO: For debug only
//                .antMatchers("/login").permitAll()
//                .antMatchers("/registerUser").permitAll()
//                .antMatchers("/processUserRegistration").permitAll()
//                .antMatchers("/").permitAll()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .loginProcessingUrl("/processLogin")
//                .successHandler(loginHandler)
//                .permitAll()
//                .and()
//                .logout().permitAll()
//                .and().csrf().disable().headers().frameOptions().disable(); // TODO: For debug only
//    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
                .antMatchers("/h2-console**").permitAll() // TODO: For debug only
                .antMatchers("/login").anonymous()
                .antMatchers("/registerUser").anonymous()
                .antMatchers("/processUserRegistration").anonymous()
                .antMatchers("/").permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/processLogin")
                .successHandler(loginHandler)
                .permitAll(false)
                .and()
                .logout().permitAll()
                .and().exceptionHandling()
                .defaultAccessDeniedHandlerFor(new AlreadyLoggedInAccessDeniedHandler(),
                        new AntPathRequestMatcher("/login"))
                .defaultAccessDeniedHandlerFor(new AlreadyLoggedInAccessDeniedHandler(),
                        new AntPathRequestMatcher("/registerUser"))
                .defaultAccessDeniedHandlerFor(new AlreadyLoggedInAccessDeniedHandler(),
                        new AntPathRequestMatcher("/processUserRegistration"))
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
