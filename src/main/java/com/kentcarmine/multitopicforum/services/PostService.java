package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.PostCreationDto;
import com.kentcarmine.multitopicforum.model.Post;
import com.kentcarmine.multitopicforum.model.TopicThread;
import com.kentcarmine.multitopicforum.model.User;

public interface PostService {
    Post addNewPostToThread(PostCreationDto postCreationDto, User creatingUser, TopicThread thread);

    Post getPostById(Long id);

    Post deletePost(Post post, User deletingUser);

    Post restorePost(Post post);

    String getGetDeletedPostUrl(Post postToDelete);

    String getRestoredPostUrl(Post postToRestore);


}
