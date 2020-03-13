package com.kentcarmine.multitopicforum.helpers;

import com.kentcarmine.multitopicforum.model.TopicThread;

import java.util.Comparator;

/**
 * Compares two TopicThreads, ordering them based on the time of their most recent post, in ascending order.
 */
public class ThreadUpdateTimeComparator implements Comparator<TopicThread> {
    @Override
    public int compare(TopicThread o1, TopicThread o2) {

        if ((o1 == null || o1.getLastPost() == null || o1.getLastPost().getPostedAt() == null) && (o2 == null || o2.getLastPost() == null || o2.getLastPost().getPostedAt() == null)) {
            return 0;
        } else if (o1 == null || o1.getLastPost() == null || o1.getLastPost().getPostedAt() == null) {
            return -1;
        } else if (o2 == null || o2.getLastPost() == null || o2.getLastPost().getPostedAt() == null) {
            return 1;
        }

        if (o1.getLastPost().getPostedAt().before(o2.getLastPost().getPostedAt())) {
            return 1;
        } else if (o1.getLastPost().getPostedAt().after(o2.getLastPost().getPostedAt())) {
            return -1;
        } else {
            return 0;
        }
    }
}
