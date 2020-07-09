package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import org.springframework.data.domain.Page;

import java.util.SortedSet;

public interface DisciplineService {

    boolean disciplineUser(UserDisciplineSubmissionDto userDisciplineSubmissionDto, User loggedInUser);

    void handleDisciplinedUser(User user) throws DisciplinedUserException;

    SortedSet<DisciplineViewDto> getActiveDisciplinesForUser(User user, User loggedInUser);

    Page<DisciplineViewDto> getInactiveDisciplineDtosForUserPaginated(User user, int pageNum, int elementsPerPage,
                                                                      User loggedInUser);

    Discipline getDisciplineByIdAndUser(Long id, User user);

    void rescindDiscipline(Discipline disciplineToRescind);

    String getLoggedInUserBannedInformationMessage(Discipline greatestDurationActiveDiscipline);

    String getLoggedInUserBannedInformationHeader(Discipline greatestDurationActiveDiscipline);
}
