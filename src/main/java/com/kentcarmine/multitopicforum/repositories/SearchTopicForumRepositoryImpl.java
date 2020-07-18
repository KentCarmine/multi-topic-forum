package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicForum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * Class that provides custom SQL queries for searching TopicForums
 */
public class SearchTopicForumRepositoryImpl extends AbstractSearchRepository implements SearchTopicForumRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Searches for any TopicForums that include all search terms in searchText in their description and name, case
     * insensitive, and returns the requested Page of those forums
     *
     * @param searchText a string of space-delimited search terms
     * @return the requested Page of TopicForums that includes all search terms in searchText in their description and
     * name, case insensitive
     */
    @Override
    public Page<TopicForum> searchTopicForumsPaginated(String searchText, Pageable page) {
        Set<String> searchTerms = splitAndEscapeSearchTerms(searchText);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TopicForum> query = criteriaBuilder.createQuery(TopicForum.class);
        Root<TopicForum> topicForumRoot = query.from(TopicForum.class);

        Path<String> namePath = topicForumRoot.get("name");
        Path<String> descriptionPath = topicForumRoot.get("description");

        Expression<String> totalTextExpr = criteriaBuilder.concat(criteriaBuilder.concat(namePath, " "), descriptionPath);

        List<Predicate> predicates = new ArrayList<>();
        for (String term : searchTerms) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(totalTextExpr), '%' + term.toLowerCase() + '%'));
        }

        CriteriaQuery<TopicForum> fullQuery = query.select(topicForumRoot)
                .where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])))
                .orderBy(criteriaBuilder.asc(topicForumRoot.get("name")));

        TypedQuery<TopicForum> typedQuery = entityManager.createQuery(fullQuery);

        int totalElements = typedQuery.getResultList().size();

        typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        typedQuery.setMaxResults(page.getPageSize());

        Page<TopicForum> result = new PageImpl<TopicForum>(typedQuery.getResultList(), page, totalElements);

        return result;
    }

    /**
     * Searches for any TopicForums that include all search terms in searchText in their description and name, case
     * insensitive
     *
     * @param searchText a string of space-delimited search terms
     * @return a list of TopicForums that includes all search terms in searchText in their description and name, case
     * insensitive
     */
    @Override
    public List<TopicForum> searchTopicForums(String searchText) {
        Set<String> searchTerms = splitAndEscapeSearchTerms(searchText);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TopicForum> query = criteriaBuilder.createQuery(TopicForum.class);
        Root<TopicForum> topicForumRoot = query.from(TopicForum.class);

        Path<String> namePath = topicForumRoot.get("name");
        Path<String> descriptionPath = topicForumRoot.get("description");

        Expression<String> totalTextExpr = criteriaBuilder.concat(criteriaBuilder.concat(namePath, " "), descriptionPath);

        List<Predicate> predicates = new ArrayList<>();
        for (String term : searchTerms) {
            predicates.add(criteriaBuilder.like(totalTextExpr, '%' + term + '%'));
        }

        query.select(topicForumRoot).where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        return entityManager.createQuery(query).getResultList();
    }
}
