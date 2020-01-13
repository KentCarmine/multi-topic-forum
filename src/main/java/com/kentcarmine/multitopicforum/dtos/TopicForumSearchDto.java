package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidSearchString;

/**
 * DTO for searching for a list of topic forums that match given search terms
 */
public class TopicForumSearchDto {

    @ValidSearchString
    private String searchText;

    public TopicForumSearchDto() {
    }

    public TopicForumSearchDto(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
