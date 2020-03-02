package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.converters.DisciplineToDisciplineViewDtoConverter;
import com.kentcarmine.multitopicforum.converters.UserDtoToUserConverter;
import com.kentcarmine.multitopicforum.converters.UserToUserRankAdjustmentDtoConverter;
import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplinedUserException;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class DisciplineServiceImpl implements DisciplineService {

    private final UserRepository userRepository;
//    private final UserDtoToUserConverter userDtoToUserConverter;
//    private final PasswordEncoder passwordEncoder;
//    private final AuthenticationService authenticationService;
//    private final VerificationTokenRepository verificationTokenRepository;
//    private final PasswordResetTokenRepository passwordResetTokenRepository;
//    private final AuthorityRepository authorityRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter;
//    private final UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter;
//    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public DisciplineServiceImpl(UserRepository userRepository, /*AuthenticationService authenticationService,*/
//                           UserDtoToUserConverter userDtoToUserConverter, PasswordEncoder passwordEncoder,
//                           VerificationTokenRepository verificationTokenRepository,
//                           PasswordResetTokenRepository passwordResetTokenRepository,
                           AuthorityRepository authorityRepository, DisciplineRepository disciplineRepository,
                           DisciplineToDisciplineViewDtoConverter disciplineToDisciplineViewDtoConverter,
//                           UserToUserRankAdjustmentDtoConverter userToUserRankAdjustmentDtoConverter,
                           /*MessageService messageService, */UserService userService) {
        this.userRepository = userRepository;
//        this.userDtoToUserConverter = userDtoToUserConverter;
//        this.passwordEncoder = passwordEncoder;
//        this.authenticationService = authenticationService;
//        this.verificationTokenRepository = verificationTokenRepository;
//        this.passwordResetTokenRepository = passwordResetTokenRepository;
//        this.authorityRepository = authorityRepository;
        this.disciplineRepository = disciplineRepository;
        this.disciplineToDisciplineViewDtoConverter = disciplineToDisciplineViewDtoConverter;
//        this.userToUserRankAdjustmentDtoConverter = userToUserRankAdjustmentDtoConverter;
//        this.messageService = messageService;
        this.userService = userService;
    }


    // TODO: Refactor into DisciplineService
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

        System.out.println("### in disciplineUser(). disciplinedUser.isBanned() = " + disciplinedUser.isBanned());
        System.out.println("### in disciplineUser(). disciplineType = " + disciplineType);

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

    // TODO: Refactor into DisciplineService
    /**
     * Throw a DisciplinedUserException if the given user has any active disciplines
     *
     * @param user the user to check for active disiciplines
     * @throws DisciplinedUserException if the given user has active disciplines
     */
    @Override
    public void handleDisciplinedUser(User user) throws DisciplinedUserException {
        if (user != null && user.isBannedOrSuspended()) {
            System.out.println("### in handleDisciplinedUser() fire exception case for " + user);
            throw new DisciplinedUserException(user);
        }
    }

    // TODO: Refactor into DisciplineService
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

    // TODO: Refactor into DisciplineService
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
                if (o1.isBan() && o2.isBan()) {
                    return 0;
                } else if (o1.isBan()) {
                    return 1;
                } else if (o2.isBan()) {
                    return -1;
                } else {
                    return o1.getDisciplinedUntil().compareTo(o2.getDisciplinedUntil());
                }
            }
        };

        return getSortedDisciplineViewDtos(user.getInactiveDisciplines(), comparator, null);
    }

    // TODO: Refactor into DisciplineService
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

    // TODO: Refactor into DisciplineService
    @Override
    @Transactional
    public void rescindDiscipline(Discipline disciplineToRescind) {
        disciplineToRescind.setRescinded(true);
        disciplineRepository.save(disciplineToRescind);
    }



    // TODO: Refactor into DisciplineService
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

            dtoSet.add(dto);
        }

        return dtoSet;
    }

    // TODO: Refactor into DisciplineService
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
        StringBuilder msgBuilder = new StringBuilder("You have been ");

        if (greatestDurationActiveDiscipline.isBan()) {
            msgBuilder.append("permanently banned.");
        } else {
            String endsAtStr = greatestDurationActiveDiscipline.getDisciplineEndTime().toString();
            msgBuilder.append("suspended. Your suspension will end at: " + endsAtStr + ".");
        }

        msgBuilder.append(" The reason given for this disciplinary action was: " + greatestDurationActiveDiscipline.getReason());

        return msgBuilder.toString();
    }
}
