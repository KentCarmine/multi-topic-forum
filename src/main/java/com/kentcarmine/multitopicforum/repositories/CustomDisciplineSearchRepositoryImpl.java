package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.util.Date;

public class CustomDisciplineSearchRepositoryImpl implements CustomDisciplineSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Discipline> findAllByDisciplinedUserAndInactive(User user, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Discipline> query = criteriaBuilder.createQuery(Discipline.class);
        Root<Discipline> disciplineRoot = query.from(Discipline.class);

        Path<User> userPath = disciplineRoot.get("disciplinedUser");
        Path<Boolean> rescindedPath = disciplineRoot.<Boolean>get("rescinded");
        Path<Date> disciplinedUntilPath = disciplineRoot.<Date>get("disciplineEnd");

        Predicate isRescinded = criteriaBuilder.isTrue(rescindedPath);
        Predicate isUser = criteriaBuilder.equal(userPath, user);
        Predicate isExpired = criteriaBuilder.lessThan(disciplinedUntilPath, Date.from(Instant.now()));

        Predicate fullPredicate = criteriaBuilder.and(isUser, criteriaBuilder.or(isRescinded, isExpired));

        CriteriaQuery<Discipline> fullQuery = query.select(disciplineRoot)
                .where(fullPredicate)
                .orderBy(criteriaBuilder.desc(disciplineRoot.get("disciplinedAt")));

        TypedQuery<Discipline> typedQuery = entityManager.createQuery(fullQuery);

        int totalElems = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        Page<Discipline> result  = new PageImpl<>(typedQuery.getResultList(), pageable, totalElems);

        return result;
    }
}
