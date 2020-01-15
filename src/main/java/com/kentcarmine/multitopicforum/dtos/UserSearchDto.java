package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidSearchString;

public class UserSearchDto {

    @ValidSearchString
    private String searchText;

    public UserSearchDto() {
    }

    public UserSearchDto(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
