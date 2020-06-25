package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DisciplineRepository extends PagingAndSortingRepository<Discipline, Long>, CustomDisciplineSearchRepository {
    Discipline findByDisciplinedUserUsername(String username);

    Discipline findByDisciplinedUser(User user);

    Page<Discipline> findAllByDisciplinedUser(User user, Pageable pageable);
}
