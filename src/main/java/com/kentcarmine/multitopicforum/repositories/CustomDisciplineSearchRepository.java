package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDisciplineSearchRepository {

    Page<Discipline> findAllByDisciplinedUserAndInactive(User user, Pageable pageable);
}
