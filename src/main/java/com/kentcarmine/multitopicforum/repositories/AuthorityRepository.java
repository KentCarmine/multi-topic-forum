package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Authority;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    void deleteByUserAndAuthority(User user, UserRole authority);
}
