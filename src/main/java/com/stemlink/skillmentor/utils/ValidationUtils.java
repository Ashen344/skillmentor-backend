package com.stemlink.skillmentor.utils;

import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ValidationUtils {

    /**
     * Validates that the session is not in the past.
     * We allow a 5-minute buffer to account for minor clock differences.
     */
    public static void validateSessionNotInPast(Date sessionAt) {
        Date now = new Date();
        // Subtract 5 minutes as a small buffer
        Calendar buffer = Calendar.getInstance();
        buffer.setTime(now);
        buffer.add(Calendar.MINUTE, -5);

        if (sessionAt.before(buffer.getTime())) {
            throw new SkillMentorException(
                    "Session date cannot be in the past", HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Validates if the mentor is available during the requested session time.
     */
    public static void validateMentorAvailability(Mentor mentor, Date sessionAt, Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            durationMinutes = 60;
        }

        Date sessionEnd = addMinutesToDate(sessionAt, durationMinutes);
        List<Session> mentorSessions = mentor.getSessions();

        for (Session existingSession : mentorSessions) {
            // Skip cancelled sessions
            if ("cancelled".equals(existingSession.getSessionStatus())) continue;

            Date existingStart = existingSession.getSessionAt();
            Date existingEnd = addMinutesToDate(existingStart, existingSession.getDurationMinutes());

            if (isTimeOverlap(sessionAt, sessionEnd, existingStart, existingEnd)) {
                throw new SkillMentorException(
                        "Mentor is not available at the requested time", HttpStatus.CONFLICT
                );
            }
        }
    }

    /**
     * Validates if the student is available during the requested session time.
     */
    public static void validateStudentAvailability(Student student, Date sessionAt, Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            durationMinutes = 60;
        }

        Date sessionEnd = addMinutesToDate(sessionAt, durationMinutes);
        List<Session> studentSessions = student.getSessions();

        for (Session existingSession : studentSessions) {
            // Skip cancelled sessions
            if ("cancelled".equals(existingSession.getSessionStatus())) continue;

            Date existingStart = existingSession.getSessionAt();
            Date existingEnd = addMinutesToDate(existingStart, existingSession.getDurationMinutes());

            if (isTimeOverlap(sessionAt, sessionEnd, existingStart, existingEnd)) {
                throw new SkillMentorException(
                        "You already have a session booked at this time", HttpStatus.CONFLICT
                );
            }
        }
    }

    /**
     * Checks if two time periods overlap.
     */
    public static boolean isTimeOverlap(Date start1, Date end1, Date start2, Date end2) {
        return start1.before(end2) && start2.before(end1);
    }

    /**
     * Adds minutes to a given date.
     */
    public static Date addMinutesToDate(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}