package com.kentcarmine.multitopicforum.bootstrap;

import com.kentcarmine.multitopicforum.model.Authority;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile({"dev", "test"})
public class Bootstrap implements CommandLineRunner {

    private UserRepository userRepository;

    @Autowired
    public Bootstrap(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createUsers();
    }

    private void createUsers() {
        UserRole[] superAdminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR, UserRole.SUPER_ADMINISTRATOR};
        User superAdmin = new User("superadmin", "{bcrypt}$2a$10$cttbmjQK2y1T/cUziaBKfuzcQ.6d2.F2jmZTuXcxuMZ.ofdKhy8iC", "superadmin@test.com");
        superAdmin.addAuthorities(superAdminRoles);
        userRepository.save(superAdmin);

        UserRole[] adminRoles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMINISTRATOR};
        User admin1 = new User("admin", "{bcrypt}$2a$10$OzkZPlR1KVnET7vNDXRSP.ww5vxB2y2134x26bM3ii0uu6oNrGsca", "admin@test.com");
        admin1.addAuthorities(adminRoles);
        userRepository.save(admin1);

        User admin2 = new User("admin2", "{bcrypt}$2a$10$5wi4HHICDfbufi4aWBIW/.il6s8X0v8pzPbn2Hn.kpeMJU1EUgLra", "admin2@test.com");
        admin2.addAuthorities(adminRoles);
        userRepository.save(admin2);

        UserRole[] modRoles = {UserRole.USER, UserRole.MODERATOR};
        User mod1 = new User("moderator", "{bcrypt}$2a$10$hJwauYHCeQnFJMk8kGfo/.zzNuH.TnhNyTCUY.mRXHFkAgGwtsJZm", "moderator@test.com");
        mod1.addAuthorities(modRoles);
        userRepository.save(mod1);

        User mod2 = new User("moderator2", "{bcrypt}$2a$10$K39s1FmJXgNG1fLY9Njptu3VvE1bxECkT3IdACffDiUerHuB9LSoi", "moderator2@test.com");
        mod2.addAuthorities(modRoles);
        userRepository.save(mod2);

        User user1 = new User("user", "{bcrypt}$2a$10$VMT4W4hEJ7xhUKXGnLboheJQYzvOQDR41KfPGQu3d4daR8TI0X3je", "user@test.com");
        user1.addAuthority(UserRole.USER);
        userRepository.save(user1);

        User user2 = new User("user2", "{bcrypt}$2a$10$7pPZby2Uu6uITXSv0bMmuOVmb2CDYVr5lewlNSvlZShjU.e4FdClm", "user2@test.com");
        user2.addAuthority(UserRole.USER);
        userRepository.save(user2);
    }
}
