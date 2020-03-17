package com.kentcarmine.multitopicforum.services;

import com.kentcarmine.multitopicforum.dtos.PostViewDto;
import com.kentcarmine.multitopicforum.dtos.TopicThreadViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service providing methods relating to the time of creation or updating threads or posts.
 */
@Service
public class TimeCalculatorServiceImpl implements TimeCalculatorService {

    private MessageService messageService;

    private String secondStr;
    private String minuteStr;
    private String hourStr;
    private String dayStr;
    private String weekStr;
    private String monthStr;
    private String yearStr;

    private String[] timeMeasurements;

    @Autowired
    public TimeCalculatorServiceImpl(MessageService messageService) {
        this.messageService = messageService;
        this.secondStr = messageService.getMessage("Label.second");
        this.minuteStr = messageService.getMessage("Label.minute");
        this.hourStr = messageService.getMessage("Label.hour");
        this.dayStr = messageService.getMessage("Label.day");
        this.weekStr = messageService.getMessage("Label.week");
        this.monthStr = messageService.getMessage("Label.month");
        this.yearStr = messageService.getMessage("Label.year");
    }

    /**
     * Get a message in human-readable form defining the amount of time (in largest applicable units) since the given
     * post was created.
     *
     * @param postViewDto the ViewDto of the post to check for creation time
     * @return a message in human-readable form defining the amount of time (in largest applicable units) since the
     * given post was created.
     */
    @Override
    public String getTimeSincePostCreationMessage(PostViewDto postViewDto) {
        final String[] timeMeasurements = getTimeMeasurements();

        final long[] elapsedList = getElapsedTimeSinceDateAsUnitsList(postViewDto.getPostedAt());

        for (int i = timeMeasurements.length - 1; i >= 0; i--) {
            String unit = timeMeasurements[i];
            long elapsed = elapsedList[i];

            if (elapsed > 0) {
                if (elapsed == 1) {
                    return messageService.getMessage("TopicThread.lastUpdated.singular", elapsed, unit);
                } else {
                    return messageService.getMessage("TopicThread.lastUpdated.plural", elapsed, unit);
                }
            }
        }

        return messageService.getMessage("TopicThread.lastUpdated.plural", 0, secondStr);
    }

    @Override
    public String getTimeSinceThreadCreationMessage(TopicThreadViewDto threadViewDto) {
        return getTimeSincePostCreationMessage(threadViewDto.getFirstPost());
    }

    @Override
    public String getTimeSinceThreadUpdatedMessage(TopicThreadViewDto threadViewDto) {
        return getTimeSincePostCreationMessage(threadViewDto.getLastPost());
    }

    /**
     * Get a list of time measurement strings for units from seconds to years.
     *
     * @return a list of time measurement strings for units from seconds to years
     */
    private String[] getTimeMeasurements() {
        if (timeMeasurements == null) {
            timeMeasurements = new String[]{secondStr, minuteStr, hourStr, dayStr, weekStr, monthStr, yearStr};
        }

        return timeMeasurements;
    }

    private long[] getElapsedTimeSinceDateAsUnitsList(Date date) {
        LocalDateTime timestamp = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(timestamp, now);
        Period period = Period.between(timestamp.toLocalDate(), now.toLocalDate());

        long seconds = duration.getSeconds();
        long mins = duration.dividedBy(Duration.ofMinutes(1));
        long hours = duration.dividedBy(Duration.ofHours(1));
        long days = duration.dividedBy(Duration.ofDays(1));
        long weeks = duration.dividedBy(Duration.ofDays(7));
        long months = period.getMonths();
        long years = period.getYears();

        return new long[]{seconds, mins, hours, days, weeks, months, years};
    }
}
