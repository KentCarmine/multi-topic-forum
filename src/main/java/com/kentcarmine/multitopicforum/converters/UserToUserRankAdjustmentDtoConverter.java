package com.kentcarmine.multitopicforum.converters;

import com.kentcarmine.multitopicforum.dtos.UserRankAdjustmentDto;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserRankAdjustmentDtoConverter implements Converter<User, UserRankAdjustmentDto> {
    @Override
    public UserRankAdjustmentDto convert(User user) {
        UserRankAdjustmentDto dto = new UserRankAdjustmentDto(user.getUsername(), user.getHighestAuthority(), user.getIncrementedRank(), user.getDecrementedRank());

        return dto;
    }
}
