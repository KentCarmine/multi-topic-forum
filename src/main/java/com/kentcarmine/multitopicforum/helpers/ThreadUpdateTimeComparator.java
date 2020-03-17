package com.kentcarmine.multitopicforum.helpers;

import com.kentcarmine.multitopicforum.model.ThreadUpdatedTimeable;

import java.util.Comparator;

/**
 * Compares two ThreadUpdatedTimeable objects, ordering them based on the time of their most recent post, in ascending order.
 */
public class ThreadUpdateTimeComparator implements Comparator<ThreadUpdatedTimeable> {

    @Override
    public int compare(ThreadUpdatedTimeable o1, ThreadUpdatedTimeable o2) {

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
