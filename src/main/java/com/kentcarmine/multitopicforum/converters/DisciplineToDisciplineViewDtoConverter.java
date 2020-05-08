package com.kentcarmine.multitopicforum.converters;

import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.model.Discipline;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DisciplineToDisciplineViewDtoConverter implements Converter<Discipline, DisciplineViewDto> {

    @Override
    public DisciplineViewDto convert(Discipline discipline) {
        DisciplineViewDto dto = new DisciplineViewDto(discipline.getId(), discipline.getDisciplinedUser().getUsername(),
                discipline.getDiscipliningUser().getUsername(), discipline.getDisciplineType(),
                discipline.getDisciplinedAt(), discipline.getDisciplineEndTime(),
                discipline.getDisciplineDurationHours(), discipline.getReason(), discipline.isRescinded());

        return dto;
    }
}
