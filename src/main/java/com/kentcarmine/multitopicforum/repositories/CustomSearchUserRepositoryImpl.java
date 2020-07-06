package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomSearchUserRepositoryImpl extends AbstractSearchRepository implements CustomSearchUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<User> findAllUsersByUsernamesLikeIgnoreCaseCustom(String searchText, Pageable pageable) {
        Set<String> searchTerms = splitAndEscapeSearchTerms(searchText);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Path<String> usernamePath = userRoot.get("username");

        List<Predicate> predicates = new ArrayList<>();
        for (String term : searchTerms) {
            Predicate usernameLike = criteriaBuilder.like(criteriaBuilder.lower(usernamePath), '%' + term.toLowerCase() + '%');
            predicates.add(usernameLike);
        }

        CriteriaQuery<User> fullQuery = query.select(userRoot)
                .where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])))
//                .orderBy(criteriaBuilder.desc(titlePath));
                .orderBy(criteriaBuilder.desc(usernamePath));

        TypedQuery<User> typedQuery = entityManager.createQuery(fullQuery);

        int totalElements = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        Page<User> result = new PageImpl<User>(typedQuery.getResultList(), pageable, totalElements);

        return result;
    }
}
