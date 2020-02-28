package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.TopicForumDtoToTopicForumConverter;
import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.model.*;
import com.kentcarmine.multitopicforum.repositories.PostRepository;
import com.kentcarmine.multitopicforum.repositories.PostVoteRepository;
import com.kentcarmine.multitopicforum.repositories.TopicForumRepository;
import com.kentcarmine.multitopicforum.repositories.TopicThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PostVoteServiceImpl implements PostVoteService {

    private final TopicForumRepository topicForumRepository;
    private final TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter;
    private final TopicThreadRepository topicThreadRepository;
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;

    @Autowired
    public PostVoteServiceImpl(TopicForumRepository topicForumRepository,
                            TopicForumDtoToTopicForumConverter topicForumDtoToTopicForumConverter,
                            TopicThreadRepository topicThreadRepository, PostRepository postRepository,
                            PostVoteRepository postVoteRepository) {
        this.topicForumRepository = topicForumRepository;
        this.topicForumDtoToTopicForumConverter = topicForumDtoToTopicForumConverter;
        this.topicThreadRepository = topicThreadRepository;
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
    }

    // TODO: Refactor into PostVoteService
    /**
     * Generates a map from Post IDs to votes made on those posts by the given user. Those values can be 1 (upvote),
     * 0 (no vote), or -1 (downvote).
     *
     * @param loggedInUser the user to check votes made by
     * @param thread the TopicThread to get the list of post IDs from
     * @return map from Post IDs to votes made on those posts by the given user
     */
    @Override
    public Map<Long, Integer> generateVoteMap(User loggedInUser, TopicThread thread) {
        Map<Long, Integer> voteMap = new HashMap<>();

        for (Post post : thread.getPosts()) {
            PostVote vote = postVoteRepository.findByUserAndPost(loggedInUser, post);
            if (vote == null) {
                voteMap.put(post.getId(), PostVoteState.NONE.getValue());
            } else {
                voteMap.put(post.getId(), vote.getPostVoteState().getValue());
            }
        }

        return voteMap;
    }

    // TODO: Refactor into PostVoteService
    /**
     * Get the PostVote made by the given user on the given post, or null if no such PostVote exists.
     * @param user the user owning the PostVote
     * @param post the post owning the PostVote
     * @return the PostVote made by the given user on the given post, or null if no such PostVote exists.
     */
    @Override
    public PostVote getPostVoteByUserAndPost(User user, Post post) {
        return postVoteRepository.findByUserAndPost(user, post);
    }

    // TODO: Refactor into PostVoteService
    /**
     * Processes submission of a PostVote by the given user on the given post with vote values in the
     * postVoteSubmissionDto. Either creates a new vote if no vote by that user on that post exists, or updates that
     * user's existing vote on that post if it has a value of NONE. Then returns data to the client indicating the
     * current number of votes on that post and if the user's vote was saved.
     *
     * @param loggedInUser The user submitting the vote
     * @param post The post the vote is on
     * @param postVoteSubmissionDto data about the vote
     * @return the response object to be sent back to the client
     */
    @Transactional
    @Override
    public PostVoteResponseDto handlePostVoteSubmission(User loggedInUser, Post post, PostVoteSubmissionDto postVoteSubmissionDto) {
        PostVoteResponseDto postVoteResponseDto;

        PostVote postVote = getPostVoteByUserAndPost(loggedInUser, post);
        if (postVote == null || postVote.getPostVoteState().equals(PostVoteState.NONE)) {
            if (postVote == null) {
                System.out.println("### Creating new vote");
                postVote = new PostVote(PostVoteState.NONE, loggedInUser, post);
            } else {
                System.out.println("### Updating existing vote");
            }

            PostVoteState voteState;
            if (postVoteSubmissionDto.getVoteValue() == 1) {
                voteState = PostVoteState.UPVOTE;
            } else if (postVoteSubmissionDto.getVoteValue() == -1) {
                voteState = PostVoteState.DOWNVOTE;
            } else {
                voteState = PostVoteState.NONE;
            }
            postVote.setPostVoteState(voteState);
            postVote = postVoteRepository.save(postVote);
            post.addPostVote(postVote);

            postVoteResponseDto = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), true, post.getVoteCount());
//            System.out.println("### Response: " + postVoteResponseDto);
        } else {
            System.out.println("### Invalid vote submission in handlePostVoteSubmission()");
            postVoteResponseDto = new PostVoteResponseDto(post.getId(), postVote.isUpvote(), postVote.isDownvote(), false, post.getVoteCount());
        }

        return postVoteResponseDto;
    }

}
