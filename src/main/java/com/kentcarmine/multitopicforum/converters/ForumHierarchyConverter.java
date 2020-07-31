package com.kentcarmine.multitopicforum.converters;

import com.kentcarmine.multitopicforum.dtos.*;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicForum;
import com.kentcarmine.multitopicforum.model.TopicThread;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.SortedSet;

/**
 * Converter that provides methods to convert TopicForums, TopicThreads, and Posts into their associated ViewDtos. Includes
 * the ability to convert nested objects.
 */
@Component
public class ForumHierarchyConverter {

    public TopicForumViewDto convertForum(TopicForum forum) {
        TopicForumViewDto dto = new TopicForumViewDto(forum.getName(), forum.getDescription());

        Set<TopicThread> threads = forum.getThreads();

        for (TopicThread thread : threads) {
            TopicThreadViewDto threadDto = convertThread(thread, dto);
            dto.addThread(threadDto);
        }

        return dto;
    }

    public TopicForumViewDtoLight convertForumLight(TopicForum forum) {
        PostViewDto postViewDto = convertPost(forum.getMostRecentPost(), null);

        Post post = forum.getMostRecentPost();
        TopicThread mostRecentUpdatedThread = null;
        if (post != null) {
            mostRecentUpdatedThread = post.getThread();
        }

        TopicThreadViewDtoLight topicThreadViewDto = convertThreadLight(mostRecentUpdatedThread, null);
        if (postViewDto != null) {
            postViewDto.setThread(topicThreadViewDto);
        }
        TopicForumViewDtoLight forumDto = new TopicForumViewDtoLight(forum.getName(), forum.getDescription(), forum.getNumThreads(), postViewDto);
        if (topicThreadViewDto != null) {
            topicThreadViewDto.setForum(forumDto);
        }

        return forumDto;
    }

    public TopicForumViewDto convertForumWithoutThreads(TopicForum forum, SortedSet<TopicThread> threads) {
        TopicForumViewDto dto = new TopicForumViewDto(forum.getName(), forum.getDescription());

        for (TopicThread thread : threads) {
            TopicThreadViewDto threadDto = convertThread(thread, dto);
            dto.addThread(threadDto);
        }

        return dto;
    }

    public TopicThreadViewDto convertThread(TopicThread thread, TopicForumViewDto forumViewDto) {
        TopicThreadViewDto dto = new TopicThreadViewDto(thread.getTitle(), forumViewDto);

        dto.setId(thread.getId());
        dto.setLockingUser(thread.getLockingUser());
        dto.setLocked(thread.isLocked());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setUpdatedAt(thread.getUpdatedAt());

        Set<Post> posts = thread.getPosts();

        for (Post p : posts) {
            dto.addPost(convertPost(p, dto));
        }

        return dto;
    }

    public TopicThreadViewDtoLight convertThreadLight(TopicThread thread, TopicForumViewDtoLight forumViewDto) {
        if (thread == null) {
            return null;
        }

        TopicThreadViewDtoLight dto = new TopicThreadViewDtoLight(thread.getTitle(), forumViewDto);
        dto.setId(thread.getId());
        dto.setLockingUser(thread.getLockingUser());
        dto.setLocked(thread.isLocked());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setUpdatedAt(thread.getUpdatedAt());

        dto.setPostCount(thread.getPostCount());
        dto.setFirstPost(convertPost(thread.getFirstPost(), dto));
        dto.setLastPost(convertPost(thread.getLastPost(), dto));

        return dto;
    }



    public PostViewDto convertPost(Post post, AbstractTopicThreadViewDto threadViewDto) {
        if (post == null) {
            return  null;
        }

        PostViewDto dto = new PostViewDto(post.getContent(), post.getPostedAt());
        dto.setId(post.getId());
        dto.setDeleted(post.isDeleted());
        dto.setDeletedAt(post.getDeletedAt());
        dto.setDeletedBy(post.getDeletedBy());
        dto.setPostedAt(post.getPostedAt());
        dto.setUser(post.getUser());
        dto.setPostVotes(post.getPostVotes());
        dto.setThread(threadViewDto);

        return dto;
    }
}
