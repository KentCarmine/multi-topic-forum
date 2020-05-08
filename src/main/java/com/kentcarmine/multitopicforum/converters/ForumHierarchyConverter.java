package com.kentcarmine.multitopicforum.converters;

import com.kentcarmine.multitopicforum.dtos.PostViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicForumViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
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

        Set<Post> posts = thread.getPosts();

        for (Post p : posts) {
            dto.addPost(convertPost(p, dto));
        }

        return dto;
    }

    public PostViewDto convertPost(Post post, TopicThreadViewDto threadViewDto) {
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
