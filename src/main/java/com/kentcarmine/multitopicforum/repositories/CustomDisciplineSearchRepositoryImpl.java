package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;
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
//        Path<Date> createdAtPath = disciplineRoot.<Date>get("disciplinedAt");
        Path<Boolean> rescindedPath = disciplineRoot.<Boolean>get("rescinded");
        Path<DisciplineType> disciplineTypePath = disciplineRoot.<DisciplineType>get("disciplineType");
        Path<Date> disciplinedUntilPath = disciplineRoot.<Date>get("disciplineEnd");

        Predicate isRescinded = criteriaBuilder.isTrue(rescindedPath);
        Predicate isUser = criteriaBuilder.equal(userPath, user);
//        Predicate isSuspended = criteriaBuilder.equal(disciplineTypePath, DisciplineType.SUSPENSION);
        Predicate isExpired = criteriaBuilder.lessThan(disciplinedUntilPath, Date.from(Instant.now()));

//        Predicate fullPredicate = criteriaBuilder.and(isUser, criteriaBuilder.or(isRescinded, criteriaBuilder.and(isSuspended, isExpired)));
        Predicate fullPredicate = criteriaBuilder.and(isUser, criteriaBuilder.or(isRescinded, isExpired));

//        List<Predicate> predList = new ArrayList<>();
//        predList.add(criteriaBuilder.or(isExpired, isRescinded));
//        predList.add(criteriaBuilder.equal(userPath, user));

        CriteriaQuery<Discipline> fullQuery = query.select(disciplineRoot)
                .where(fullPredicate)
//                .where(criteriaBuilder.and(predList.toArray(new Predicate[predList.size()])))
                .orderBy(criteriaBuilder.desc(disciplineRoot.get("disciplinedAt")));

        TypedQuery<Discipline> typedQuery = entityManager.createQuery(fullQuery);

        int totalElems = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        Page<Discipline> result  = new PageImpl<>(typedQuery.getResultList(), pageable, totalElems);

        return result;
    }
}
