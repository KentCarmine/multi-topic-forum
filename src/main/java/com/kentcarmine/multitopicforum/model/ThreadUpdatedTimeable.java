package com.kentcarmine.multitopicforum.model;

/**
 * Interface that defines objects that can get a first and last post.
 */
public interface ThreadUpdatedTimeable {
    PostUpdatedTimable getFirstPost();

    PostUpdatedTimable getLastPost();
}
