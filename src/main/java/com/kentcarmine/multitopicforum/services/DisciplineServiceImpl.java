package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.DisciplineRepository;
import com.kentcarmine.multitopicforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
public class DisciplineServiceImpl implements DisciplineService {

    private final UserRepository userRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter;
    private final UserService userService;
    private final MessageService messageService;

    @Autowired
    public DisciplineServiceImpl(UserRepository userRepository, DisciplineRepository disciplineRepository,
                                 DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter,
                                 UserService userService, MessageService messageService) {
        this.userRepository = userRepository;
        this.disciplineRepository = disciplineRepository;
        this.disciplineToDisciplineViewDtoConverter = disciplineToDisciplineViewDtoConverter;
        this.userService = userService;
        this.messageService = messageService;
    }

    /**
     * Creates a new discipline entry described by the UserDisciplineSubmissionDto and created by the loggedInUser.
     *
     * @param userDisciplineSubmissionDto describes the discipinary action taken and against which user
     * @param loggedInUser the logged in user
     */
    @Override
    @Transactional
    public boolean disciplineUser(UserDisciplineSubmissionDto userDisciplineSubmissionDto, User loggedInUser) {
        User disciplinedUser = userService.getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        DisciplineType disciplineType = userDisciplineSubmissionDto.isBan() ? DisciplineType.BAN : DisciplineType.SUSPENSION;

//        System.out.println("### in disciplineUser(). disciplinedUser.isBanned() = " + disciplinedUser.isBanned());
//        System.out.println("### in disciplineUser(). disciplineType = " + disciplineType);

        if (disciplineType.equals(DisciplineType.BAN) && disciplinedUser.isBanned()) {
            return false;
        }

        Discipline discipline = new Discipline(disciplinedUser, loggedInUser, disciplineType, Date.from(Instant.now()),
                userDisciplineSubmissionDto.getReason());

        if (disciplineType.equals(DisciplineType.SUSPENSION)) {
            discipline.setDisciplineDurationHours(Integer.parseInt(userDisciplineSubmissionDto.getSuspensionHours()));
        }

        discipline = disciplineRepository.save(discipline);

        disciplinedUser.addDiscipline(discipline);

        disciplinedUser = userRepository.save(disciplinedUser);

        return true;
    }

    /**
     * Throw a DisciplinedUserException if the given user has any active disciplines
     *
     * @param user the user to check for active disiciplines
     * @throws DisciplinedUserException if the given user has active disciplines
     */
    @Override
    public void handleDisciplinedUser(User user) throws DisciplinedUserException {
        if (user != null && user.isBannedOrSuspended()) {
//            System.out.println("### in handleDisciplinedUser() fire exception case for " + user);
            throw new DisciplinedUserException(user);
        }
    }

    /**
     * Get a SortedSet of DisciplineViewDtos for all the given user's active disciplines.
     *
     * @param user the user to get active disciplines for
     * @param loggedInUser the logged in user
     * @return  SortedSet of DisciplineViewDtos for all the given user's active disciplines
     */
    @Override
    public SortedSet<DisciplineViewDto> getActiveDisciplinesForUser(User user, User loggedInUser) {
        Comparator<DisciplineViewDto> comparator = new Comparator<DisciplineViewDto>() {
            @Override
            public int compare(DisciplineViewDto o1, DisciplineViewDto o2) {
                if (o1.isBan() && o2.isBan()) {
                    return 0;
                } else if (o1.isBan()) {
                    return 1;
                } else if (o2.isBan()) {
                    return -1;
                }

                if (Integer.parseInt(o1.getDisciplineDuration()) > Integer.parseInt(o2.getDisciplineDuration())) {
                    return 1;
                } else if (Integer.parseInt(o2.getDisciplineDuration()) > Integer.parseInt(o1.getDisciplineDuration())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        return getSortedDisciplineViewDtos(user.getActiveDisciplines(), comparator, loggedInUser);
    }

    /**
     * Get a SortedSet of DisciplineViewDtos for all the given user's inactive disciplines.
     *
     * @param user the user to get inactive disciplines for
     * @return  SortedSet of DisciplineViewDtos for all the given user's inactive disciplines
     */
    @Override
    public SortedSet<DisciplineViewDto> getInactiveDisciplinesForUser(User user) {
        Comparator<DisciplineViewDto> comparator = new Comparator<DisciplineViewDto>() {
            @Override
            public int compare(DisciplineViewDto o1, DisciplineViewDto o2) {
                if (o1.getDisciplinedAt().equals(o2.getDisciplinedAt())) {
                    return o1.getId().compareTo(o2.getId()); // Prevent set exclusion due to identical discipline times
                }
                return o1.getDisciplinedAt().compareTo(o2.getDisciplinedAt());
            }
        };

        comparator = comparator.reversed();

        SortedSet<DisciplineViewDto> dtoSet = getSortedDisciplineViewDtos(user.getInactiveDisciplines(), comparator, null);
        return dtoSet;
    }

    /**
     * Find the discipline object with the given ID and associated with the given user. Returns null if no such object
     * exists.
     *
     * @param id the id of the Discipline object to find
     * @param user the user being Disciplined by the object with the given id
     * @return the Discipline object, or null
     */
    @Override
    public Discipline getDisciplineByIdAndUser(Long id, User user) {
        Optional<Discipline> discOpt = disciplineRepository.findById(id);

        if (discOpt.isEmpty() || user == null) {
            return null;
        }

        Discipline discipline = discOpt.get();

        if (!discipline.getDisciplinedUser().equals(user)) {
            return null;
        }

        return discipline;
    }

    @Override
    @Transactional
    public void rescindDiscipline(Discipline disciplineToRescind) {
        disciplineToRescind.setRescinded(true);
        disciplineRepository.save(disciplineToRescind);
    }

    /**
     * Returns a message that informs they user that they have been disciplined, the reason for this, and the
     * discipline's duration.
     *
     * @param greatestDurationActiveDiscipline the discipline with the greatest duration for the logged in user
     * @return a message that informs they user that they have been disciplined, the reason for this, and the
     * discipline's duration.
     */
    @Override
    public String getLoggedInUserBannedInformationMessage(Discipline greatestDurationActiveDiscipline) {
        String msg;

        if (greatestDurationActiveDiscipline.isBan()) {
            msg = messageService.getMessage("Discipline.ban.advisory", greatestDurationActiveDiscipline.getReason());
        } else {
            String endsAtStr = greatestDurationActiveDiscipline.getDisciplineEndTime().toString();
            msg = messageService.getMessage("Discipline.suspension.advisory", endsAtStr, greatestDurationActiveDiscipline.getReason());
        }

        return msg;
    }

    @Override
    public String getLoggedInUserBannedInformationHeader(Discipline greatestDurationActiveDiscipline) {
        String msg;

        if (greatestDurationActiveDiscipline.isBan()) {
            msg = messageService.getMessage("Discipline.ban.header");
        } else {
            msg = messageService.getMessage("Discipline.suspension.header");
        }

        return msg;
    }

    /**
     * Helper method that converts a Set of Disciplines into a SortedSet of DisciplineViewDtos sorted by duration.
     *
     * @param disciplines the set of Disciplines to convert
     * @return a SortedSet of DisciplineViewDtos sorted by duration
     */
    private SortedSet<DisciplineViewDto> getSortedDisciplineViewDtos(Set<Discipline> disciplines, Comparator<DisciplineViewDto> comparator, User loggedInUser) {
        SortedSet<DisciplineViewDto> dtoSet = new TreeSet<>(comparator);

        for (Discipline d : disciplines) {
            DisciplineViewDto dto = disciplineToDisciplineViewDtoConverter.convert(d);

            dto.setCanRescind(loggedInUser != null && (loggedInUser.equals(d.getDiscipliningUser())
                    || loggedInUser.isHigherAuthority(d.getDiscipliningUser())));

            if (d.isBan()) {
                dto.setDisciplinedUntilString(messageService.getMessage("Discipline.disciplinedUntil.ban"));
            }

            dtoSet.add(dto);
        }

        return dtoSet;
    }
}
