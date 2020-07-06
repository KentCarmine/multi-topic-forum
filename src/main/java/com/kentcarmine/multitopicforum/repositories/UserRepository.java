package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String>, CustomSearchUserRepository {
    User findByEmail(String email);

    User findByUsername(String username);

    List<User> findByUsernameLikeIgnoreCase(String searchText);

}
