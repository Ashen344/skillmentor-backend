package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.respositories.SessionRepository;
import com.stemlink.skillmentor.respositories.StudentRepository;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import com.stemlink.skillmentor.dto.SessionDTO;
import com.stemlink.skillmentor.dto.SessionUpdateDTO;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import com.stemlink.skillmentor.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    public Session createNewSession(SessionDTO sessionDTO) {
        try {
            Student student = studentRepository.findById(sessionDTO.getStudentId()).orElseThrow(
                    () -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND)
            );
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionDTO.getMentorId())).orElseThrow(
                    () -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND)
            );
            Subject subject = subjectRepository.findById(sessionDTO.getSubjectId()).orElseThrow(
                    () -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND)
            );

            ValidationUtils.validateMentorAvailability(mentor, sessionDTO.getSessionAt(), sessionDTO.getDurationMinutes());
            ValidationUtils.validateStudentAvailability(student, sessionDTO.getSessionAt(), sessionDTO.getDurationMinutes());

            Session session = modelMapper.map(sessionDTO, Session.class);
            session.setStudent(student);
            session.setMentor(mentor);
            session.setSubject(subject);

            return sessionRepository.save(session);
        } catch (SkillMentorException skillMentorException) {
            log.error("Dependencies not found to map: {}, Failed to create new session", skillMentorException.getMessage());
            throw skillMentorException;
        } catch (Exception exception) {
            log.error("Failed to create session", exception);
            throw new SkillMentorException("Failed to create new session", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public Session getSessionById(Long id) {
        return sessionRepository.findById(id).get();
    }

    public Session updateSessionById(Long id, SessionDTO updatedSessionDTO) {
        Session session = sessionRepository.findById(id).get();
        modelMapper.map(updatedSessionDTO, session);

        if (updatedSessionDTO.getStudentId() != null) {
            Student student = studentRepository.findById(updatedSessionDTO.getStudentId()).get();
            session.setStudent(student);
        }
        if (updatedSessionDTO.getMentorId() != null) {
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(updatedSessionDTO.getMentorId()))
                    .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));
            session.setMentor(mentor);
        }
        if (updatedSessionDTO.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updatedSessionDTO.getSubjectId()).get();
            session.setSubject(subject);
        }

        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

    public Session enrollSession(UserPrincipal userPrincipal, SessionDTO sessionDTO) {
        // Find student by email from JWT, or auto-create on first enrollment
        Student student = studentRepository.findByEmail(userPrincipal.getEmail())
                .orElseGet(() -> {
                    Student s = new Student();
                    s.setStudentId(userPrincipal.getId());
                    s.setEmail(userPrincipal.getEmail());
                    s.setFirstName(userPrincipal.getFirstName());
                    s.setLastName(userPrincipal.getLastName());
                    return studentRepository.save(s);
                });

        Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionDTO.getMentorId()))
                .orElseThrow(() -> new SkillMentorException(
                        "Mentor not found with mentorId: " + sessionDTO.getMentorId(), HttpStatus.NOT_FOUND));

        Subject subject = subjectRepository.findById(sessionDTO.getSubjectId())
                .orElseThrow(() -> new SkillMentorException(
                        "Subject not found with id: " + sessionDTO.getSubjectId(), HttpStatus.NOT_FOUND));

        Date sessionAt = sessionDTO.getSessionAt();
        int duration = sessionDTO.getDurationMinutes() != null ? sessionDTO.getDurationMinutes() : 60;
        Date sessionEnd = ValidationUtils.addMinutesToDate(sessionAt, duration);

        // 1. Reject sessions in the past
        ValidationUtils.validateSessionNotInPast(sessionAt);

        // 2. Reject if student already has an overlapping session with this mentor
        boolean mentorConflict = sessionRepository
                .findActiveSessionsForStudentAndMentor(userPrincipal.getEmail(), mentor.getId())
                .stream()
                .anyMatch(s -> ValidationUtils.isTimeOverlap(
                        sessionAt, sessionEnd,
                        s.getSessionAt(),
                        ValidationUtils.addMinutesToDate(s.getSessionAt(), s.getDurationMinutes())
                ));
        if (mentorConflict) {
            throw new SkillMentorException(
                    "You already have a session booked with this mentor at an overlapping time",
                    HttpStatus.CONFLICT
            );
        }

        // 3. Reject if student already booked this subject at an overlapping time
        boolean subjectConflict = sessionRepository
                .findActiveSessionsForStudentAndSubject(userPrincipal.getEmail(), subject.getId())
                .stream()
                .anyMatch(s -> ValidationUtils.isTimeOverlap(
                        sessionAt, sessionEnd,
                        s.getSessionAt(),
                        ValidationUtils.addMinutesToDate(s.getSessionAt(), s.getDurationMinutes())
                ));
        if (subjectConflict) {
            throw new SkillMentorException(
                    "You already have a session booked for this subject at an overlapping time",
                    HttpStatus.CONFLICT
            );
        }

        // 4. Check mentor availability (no double-booking the mentor's time)
        ValidationUtils.validateMentorAvailability(mentor, sessionAt, duration);

        Session session = new Session();
        session.setStudent(student);
        session.setMentor(mentor);
        session.setSubject(subject);
        session.setSessionAt(sessionAt);
        session.setDurationMinutes(duration);
        session.setSessionStatus("scheduled");
        session.setPaymentStatus("pending");

        return sessionRepository.save(session);
    }

    public List<Session> getSessionsByStudentEmail(String email) {
        return sessionRepository.findByStudent_Email(email);
    }

    // Admin: update only the status fields or meeting link
    public Session updateSessionStatus(Long id, SessionUpdateDTO updateDTO) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SkillMentorException(
                        "Session not found", HttpStatus.NOT_FOUND));

        if (updateDTO.getPaymentStatus() != null) {
            session.setPaymentStatus(updateDTO.getPaymentStatus());
        }
        if (updateDTO.getSessionStatus() != null) {
            session.setSessionStatus(updateDTO.getSessionStatus());
        }
        if (updateDTO.getMeetingLink() != null) {
            session.setMeetingLink(updateDTO.getMeetingLink());
        }

        return sessionRepository.save(session);
    }
}