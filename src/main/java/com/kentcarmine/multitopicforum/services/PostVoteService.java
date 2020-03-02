package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.PostVoteResponseDto;
import com.kentcarmine.multitopicforum.dtos.PostVoteSubmissionDto;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;

import java.util.Map;

public interface PostVoteService {
    Map<Long, Integer> generateVoteMap(User loggedInUser, TopicThread thread);

    PostVote getPostVoteByUserAndPost(User user, Post post);

    PostVoteResponseDto handlePostVoteSubmission(User loggedInUser, Post post, PostVoteSubmissionDto postVoteSubmissionDto);
}
