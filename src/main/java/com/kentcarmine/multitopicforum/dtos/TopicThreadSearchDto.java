package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidSearchString;

/**
 * DTO used for searching for threads in a given topic forum
 */
public class TopicThreadSearchDto {

    @ValidSearchString
    private String searchText;

    public TopicThreadSearchDto() {
    }

    public TopicThreadSearchDto(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
