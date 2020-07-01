package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomSearchUserRepository {
    Page<User> findAllUsersByUsernamesLikeIgnoreCaseCustom(String searchText, Pageable pageable);
}
