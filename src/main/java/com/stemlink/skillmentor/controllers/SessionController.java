package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.SessionDTO;
import com.stemlink.skillmentor.dto.SessionUpdateDTO;
import com.stemlink.skillmentor.dto.response.AdminSessionResponseDTO;
import com.stemlink.skillmentor.dto.response.SessionResponseDTO;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/sessions")
@RequiredArgsConstructor
@Validated
public class SessionController extends AbstractController {

    private final SessionService sessionService;

    // ----- Admin endpoints -----

    // GET /api/v1/sessions → returns ALL sessions (admin only, enforced in SecurityConfig)
    @GetMapping
    public ResponseEntity<List<AdminSessionResponseDTO>> getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        List<AdminSessionResponseDTO> response = sessions.stream()
                .map(this::toAdminSessionResponseDTO)
                .collect(Collectors.toList());
        return sendOkResponse(response);
    }

    // PUT /api/v1/sessions/{id} → admin updates status or meeting link
    @PutMapping("{id}")
    public ResponseEntity<AdminSessionResponseDTO> updateSession(
            @PathVariable Long id,
            @RequestBody SessionUpdateDTO updateDTO) {
        Session session = sessionService.updateSessionStatus(id, updateDTO);
        return sendOkResponse(toAdminSessionResponseDTO(session));
    }

    // ----- Student endpoints -----

    // POST /api/v1/sessions/enroll → student books a session
    @PostMapping("/enroll")
    public ResponseEntity<SessionResponseDTO> enroll(
            @RequestBody SessionDTO sessionDTO,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Session session = sessionService.enrollSession(userPrincipal, sessionDTO);
        return sendCreatedResponse(toSessionResponseDTO(session));
    }

    // GET /api/v1/sessions/my-sessions → student views their own sessions
    @GetMapping("/my-sessions")
    public ResponseEntity<List<SessionResponseDTO>> getMySessions(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Session> sessions = sessionService.getSessionsByStudentEmail(userPrincipal.getEmail());
        List<SessionResponseDTO> response = sessions.stream()
                .map(this::toSessionResponseDTO)
                .collect(Collectors.toList());
        return sendOkResponse(response);
    }

    // ----- Private helpers to map Session → DTO -----

    // Maps a session to the student-facing DTO (no student info exposed)
    private SessionResponseDTO toSessionResponseDTO(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setMentorName(session.getMentor().getFirstName() + " " + session.getMentor().getLastName());
        dto.setMentorProfileImageUrl(session.getMentor().getProfileImageUrl());
        dto.setSubjectName(session.getSubject().getSubjectName());
        dto.setSessionAt(session.getSessionAt());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setSessionStatus(session.getSessionStatus());
        dto.setPaymentStatus(session.getPaymentStatus());
        dto.setMeetingLink(session.getMeetingLink());
        return dto;
    }

    // Maps a session to the admin-facing DTO (includes student name/email)
    private AdminSessionResponseDTO toAdminSessionResponseDTO(Session session) {
        AdminSessionResponseDTO dto = new AdminSessionResponseDTO();
        dto.setId(session.getId());
        dto.setStudentName(session.getStudent().getFirstName() + " " + session.getStudent().getLastName());
        dto.setStudentEmail(session.getStudent().getEmail());
        dto.setMentorName(session.getMentor().getFirstName() + " " + session.getMentor().getLastName());
        dto.setSubjectName(session.getSubject().getSubjectName());
        dto.setSessionAt(session.getSessionAt());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setSessionStatus(session.getSessionStatus());
        dto.setPaymentStatus(session.getPaymentStatus());
        dto.setMeetingLink(session.getMeetingLink());
        return dto;
    }
}