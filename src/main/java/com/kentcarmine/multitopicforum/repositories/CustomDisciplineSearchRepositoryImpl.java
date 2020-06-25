package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

public class CustomDisciplineSearchRepositoryImpl implements CustomDisciplineSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Discipline> findAllByDisciplinedUserAndInactive(User user, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Discipline> query = criteriaBuilder.createQuery(Discipline.class);
        Root<Discipline> disciplineRoot = query.from(Discipline.class);

        Path<User> userPath = disciplineRoot.get("disciplinedUser");
        Path<String> usernamePath = userPath.get("username");
        // TODO: Continue Filling in



        return null;
    }
}
