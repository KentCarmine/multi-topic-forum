package com.kentcarmine.multitopicforum.repositories;

import com.kentcarmine.multitopicforum.model.TopicThread;
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

public class SearchTopicThreadRepositoryImpl extends AbstractSearchRepository implements SearchTopicThreadRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<TopicThread> searchForTopicThreadsInForum(String forumName, String searchText, Pageable pageable) {
        Set<String> searchTerms = splitAndEscapeSearchTerms(searchText);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TopicThread> query = criteriaBuilder.createQuery(TopicThread.class);
        Root<TopicThread> topicThreadRoot = query.from(TopicThread.class);

        Path<String> titlePath = topicThreadRoot.get("title");
        Path<String> forumNamePath = topicThreadRoot.get("forum").get("name");

        List<Predicate> predicates = new ArrayList<>();
        for (String term : searchTerms) {
            Predicate forumNameEquals = criteriaBuilder.equal(forumNamePath, forumName);
            Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(titlePath), '%' + term.toLowerCase() + '%');
            predicates.add(criteriaBuilder.and(titleLike, forumNameEquals));
        }

        CriteriaQuery<TopicThread> fullQuery = query.select(topicThreadRoot)
                .where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])))
                .orderBy(criteriaBuilder.desc(titlePath));

        TypedQuery<TopicThread> typedQuery = entityManager.createQuery(fullQuery);

        int totalElements = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        Page<TopicThread> result = new PageImpl<TopicThread>(typedQuery.getResultList(), pageable, totalElements);

        return result;
    }
}
