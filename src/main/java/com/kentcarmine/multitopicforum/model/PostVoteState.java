package com.kentcarmine.multitopicforum.model;

/**
 * Models the state of an Upvote or Downvote.
 */
public enum PostVoteState {
    UPVOTE (1),
    NONE (0),
    DOWNVOTE (-1);

    private int value;

    PostVoteState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isUpvote() {
        return value == UPVOTE.getValue();
    }

    public boolean isDownvote() {
        return value == DOWNVOTE.getValue();
    }

    public boolean isNoVote() {
        return value == NONE.getValue();
    }
}
