package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    User findByEmail(String email);

    User findByUsername(String username);

    List<User> findByUsernameLikeIgnoreCase(String searchText);
}
