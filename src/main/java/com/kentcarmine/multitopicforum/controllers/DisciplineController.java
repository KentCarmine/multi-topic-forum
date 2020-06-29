package com.kentcarmine.multitopicforum.controllers;

import com.kentcarmine.multitopicforum.dtos.DisciplineViewDto;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.exceptions.DisciplineNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.InsufficientAuthorityException;
import com.kentcarmine.multitopicforum.exceptions.PageNotFoundException;
import com.kentcarmine.multitopicforum.exceptions.UserNotFoundException;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.User;
import com.kentcarmine.multitopicforum.services.DisciplineService;
import com.kentcarmine.multitopicforum.services.MessageService;
import com.kentcarmine.multitopicforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.SortedSet;

/**
 * Controller that handles discipline-related requests.
 */
@Controller
public class DisciplineController {

    @Value("${spring.data.web.pageable.default-page-size}")
    private int resultsPerPage;

    private final UserService userService;
    private final DisciplineService disciplineService;

    @Autowired
    public DisciplineController(UserService userService, DisciplineService disciplineService) {
        this.userService = userService;
        this.disciplineService = disciplineService;
    }

    /**
     * Displays the page for managing disciplinary action against the user with the given name. Allows users with
     * sufficient permissions to ban/suspend that user or reverse active bans or suspensions. Also displays the user's
     * disciplinary history
     */
    @GetMapping("/manageUserDiscipline/{username}")
    public String showManageUserDisciplinePage(@PathVariable String username, Model model,
                                               @RequestParam(required = false, defaultValue = "1") int page) {
        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        User user = userService.getUser(username);

        if (user == null) {
            throw new UserNotFoundException("Exception.user.notfound", username);
        }

        if (!loggedInUser.isHigherAuthority(user)) {
            throw new InsufficientAuthorityException();
        }

        SortedSet<DisciplineViewDto> activeDisciplines = disciplineService.getActiveDisciplinesForUser(user, loggedInUser);
//        SortedSet<DisciplineViewDto> inactiveDisciplines = disciplineService.getInactiveDisciplinesForUser(user);
        Page<DisciplineViewDto> inactiveDisciplines = disciplineService.getInactiveDisciplineDtosForUserPaginated(user,
                page, resultsPerPage, loggedInUser);

        if (inactiveDisciplines == null) {
            throw new PageNotFoundException();
        }

        UserDisciplineSubmissionDto userDisciplineSubmissionDto = new UserDisciplineSubmissionDto();
        userDisciplineSubmissionDto.setDisciplinedUsername(username);

        model.addAttribute("userDisciplineSubmissionDto", userDisciplineSubmissionDto);
        model.addAttribute("activeDisciplines", activeDisciplines);
        model.addAttribute("inactiveDisciplines", inactiveDisciplines);

        return "user-discipline-page";
    }

    /**
     * Handles processing of a user discipline submission.
     */
    @PostMapping("/processCreateUserDiscipline")
    public ModelAndView processUserDisciplineSubmission(@Valid UserDisciplineSubmissionDto userDisciplineSubmissionDto, BindingResult bindingResult) {
        System.out.println("### in processUserDisciplineSubmission().");
        ModelAndView mv;

        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        updateDisciplineSubmissionBindingResult(userDisciplineSubmissionDto, bindingResult);

        User disciplinedUser = userService.getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        if (disciplinedUser == null) {
//            String msg = messageService.getMessage("Exception.user.notfound", userDisciplineSubmissionDto.getDisciplinedUsername());
//            throw new UserNotFoundException(msg);
            throw new UserNotFoundException("Exception.user.notfound", userDisciplineSubmissionDto.getDisciplinedUsername());
        }

        if (bindingResult.hasErrors()) {
//            System.out.println("### in processUserDisciplineSubmission(). bindingResult.hasErrors() case");
            SortedSet<DisciplineViewDto> activeDisciplines = disciplineService.getActiveDisciplinesForUser(disciplinedUser, loggedInUser);
            SortedSet<DisciplineViewDto> inactiveDisciplines = disciplineService.getInactiveDisciplinesForUser(disciplinedUser);

            mv = new ModelAndView("user-discipline-page", "userDisciplineSubmissionDto", userDisciplineSubmissionDto);
            mv.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            mv.addObject("activeDisciplines", activeDisciplines);
            mv.addObject("inactiveDisciplines", inactiveDisciplines);

            return mv;
        }

//        System.out.println("### in processUserDisciplineSubmission(). pre userService.disciplineUser() case");
        boolean successfulBan = disciplineService.disciplineUser(userDisciplineSubmissionDto, loggedInUser);

        String url = "redirect:/users/" + disciplinedUser.getUsername();
        if (successfulBan) {
            url = url + "?userDisciplined";
        } else {
            url = url + "?userAlreadyBanned";
        }

//        System.out.println("### in processUserDisciplineSubmission(). Url = " + url);
        mv = new ModelAndView(url);
        return mv;
    }

    /**
     * Shows the discipline status page for the given user, informing them of their disciplinary status (if there is one
     * active), or redirecting them to their home page if there is not. If there is a disciplinary status, also forcibly
     * logs the user out and clears their remember me cookie.
     */
    @GetMapping("/showDisciplineInfo/{username}")
    public String showDisciplineInfoPage(@PathVariable String username, Model model, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getUser(username);
        User loggedInUser = userService.getLoggedInUser();

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!user.equals(loggedInUser)) {
            return "redirect:/showDisciplineInfo/" + loggedInUser.getUsername();
        }

        if (!loggedInUser.isBannedOrSuspended()) {
            return "redirect:/login";
        }

        Discipline greatestDurationActiveDiscipline = loggedInUser.getGreatestDurationActiveDiscipline();
        String header = disciplineService.getLoggedInUserBannedInformationHeader(greatestDurationActiveDiscipline);
        String message = disciplineService.getLoggedInUserBannedInformationMessage(greatestDurationActiveDiscipline);

        model.addAttribute("username", username);
        model.addAttribute("header", header);
        model.addAttribute("message", message);

        userService.forceLogOut(loggedInUser, request, response);

        return "user-discipline-info-page";
    }

    /**
     * Handles rescinding a discipline with the given id and associated with the user with the given username, provided
     * the logged in user has the authority to do so.
     */
    @PostMapping("/rescindDiscipline/{username}/{id}")
    public String processRescindDiscipline(@PathVariable String username, @PathVariable Long id) {
//        System.out.println("### in processRescindDiscipline. username = " + username + ", id = " + id);

        User loggedInUser = userService.getLoggedInUser();
        disciplineService.handleDisciplinedUser(loggedInUser);

        User disciplinedUser = userService.getUser(username);
        if (disciplinedUser == null) {
//            String msg = messageService.getMessage("Exception.user.notfound", username);
//            String msg = messageService.getMessage("Exception.user.notfound");
//            System.out.println("### in processRescindDiscipline(). msg = " + msg);
//            throw new UserNotFoundException(msg);
            throw new UserNotFoundException("Exception.user.notfound", username);
        }

        Discipline disciplineToRescind = disciplineService.getDisciplineByIdAndUser(id, disciplinedUser);
        if (disciplineToRescind == null) {
//            System.out.println("Error in processRescindDiscipline. disciplineToRescind is null");
//            throw new DisciplineNotFoundException("Discipline to rescind was not found");
            throw new DisciplineNotFoundException();
        }

        if (!loggedInUser.equals(disciplineToRescind.getDiscipliningUser()) && !loggedInUser.isHigherAuthority(disciplineToRescind.getDiscipliningUser())) {
//            throw new InsufficientAuthorityException("Insufficient authority to rescind discipline");
            throw new InsufficientAuthorityException();
        }

        disciplineService.rescindDiscipline(disciplineToRescind);

        return "redirect:/manageUserDiscipline/" + disciplinedUser.getUsername();
    }

    private BindingResult updateDisciplineSubmissionBindingResult(UserDisciplineSubmissionDto userDisciplineSubmissionDto, BindingResult bindingResult) {
        User loggedInUser = userService.getLoggedInUser();
        User disciplinedUser = userService.getUser(userDisciplineSubmissionDto.getDisciplinedUsername());

        if (disciplinedUser == null) {
//            System.out.println("### in updateDisciplineSubmissionBindingResult(). disciplinedUser == null");
//            bindingResult.rejectValue("disciplinedUsername", "Exception.user.notfound");
//            bindingResult.rejectValue("disciplinedUsername", null, messageService.getMessage("Exception.user.notfound", userDisciplineSubmissionDto.getDisciplinedUsername()));
//            bindingResult.rejectValue("disciplinedUsername", null, "Could not find user " + userDisciplineSubmissionDto.getDisciplinedUsername());
            bindingResult.rejectValue("disciplinedUsername", "Exception.user.notfound",
                    new String[]{userDisciplineSubmissionDto.getDisciplinedUsername()},
                    "Could not find user " + userDisciplineSubmissionDto.getDisciplinedUsername());
        }

        if (loggedInUser == null || disciplinedUser == null || !loggedInUser.isHigherAuthority(disciplinedUser)) {
//            bindingResult.reject("insufficientAuthority", null, "You do not have the authority to discipline this user");
            bindingResult.reject("Exception.authority.insufficient", "You do not have the authority to discipline this user");
        }

        return bindingResult;
    }

}
