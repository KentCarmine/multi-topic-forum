package com.kentcarmine.multitopicforum.helpers;

import com.kentcarmine.multitopicforum.model.Post;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ReverseDateOrderPostComparator implements Comparator<Post> {
    @Override
    public int compare(Post o1, Post o2) {
        long o1Time = o1.getPostedAt().getTime();
        long o2Time = o2.getPostedAt().getTime();

        if (o1Time > o2Time) {
            return -1;
        } else if (o1Time < o2Time) {
            return 1;
        } else {
            return 0;
        }
    }
}
