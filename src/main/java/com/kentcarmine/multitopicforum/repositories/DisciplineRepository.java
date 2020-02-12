package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
    Discipline findByDisciplinedUserUsername(String username);

    Discipline findByDisciplinedUser(User user);
}
