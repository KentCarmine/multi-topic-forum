package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;
import com.kentcarmine.multitopicforum.helpers.ThreadUpdateTimeComparator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.SortedSet;
import java.util.TreeSet;

public class TopicForumViewDto {

    @NotBlank(message = "{Forum.name.notBlank}")
    @Size(min=4, message="{Forum.name.minSize}")
    @ValidCharacters(message = "{Forum.name.validChars}")
    private String name;

    @NotBlank(message = "{Forum.description.notBlank}")
    @Size(min = 1, max = 500, message = "{Forum.description.length}")
    private String description;

//    @SortComparator(ThreadUpdateTimeComparator.class)
    private SortedSet<TopicThreadViewDto> threads;

    public TopicForumViewDto() {
        this.threads = new TreeSet<>(new ThreadUpdateTimeComparator());
    }


    public TopicForumViewDto(@Size(min=4, message="{Forum.name.minSize}") String name, String description) {
        this.name = name;
        this.description = description;
        this.threads = new TreeSet<>(new ThreadUpdateTimeComparator());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SortedSet<TopicThreadViewDto> getThreads() {
        return threads;
    }

    public void setThreads(SortedSet<TopicThreadViewDto> threads) {
        this.threads = threads;
    }

    public void addThread(TopicThreadViewDto thread) {
        this.threads.add(thread);
    }

    @Override
    public String toString() {
        return "TopicForumViewDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", threads=" + threads +
                '}';
    }
}
