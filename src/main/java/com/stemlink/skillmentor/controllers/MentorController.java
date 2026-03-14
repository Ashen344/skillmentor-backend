package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.MentorDTO;
import com.stemlink.skillmentor.dto.response.MentorProfileDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.respositories.SessionRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.stemlink.skillmentor.constants.UserRoles.*;

@RestController
@RequestMapping(path = "/api/v1/mentors")
@RequiredArgsConstructor
@Validated
public class MentorController extends AbstractController {

    private final MentorService mentorService;
    private final ModelMapper modelMapper;
    private final SubjectRepository subjectRepository;
    private final SessionRepository sessionRepository;

    // GET /api/v1/mentors — paginated list (public)
    @GetMapping
    public ResponseEntity<Page<Mentor>> getAllMentors(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        Page<Mentor> mentors = mentorService.getAllMentors(name, pageable);
        return sendOkResponse(mentors);
    }

    // GET /api/v1/mentors/{id}/profile — full profile with subjects + enrollment counts (public)
    @GetMapping("{id}/profile")
    public ResponseEntity<MentorProfileDTO> getMentorProfile(@PathVariable Long id) {
        Mentor mentor = mentorService.getMentorById(id);

        // Build the profile DTO from the mentor entity
        MentorProfileDTO profile = new MentorProfileDTO();
        profile.setId(mentor.getId());
        profile.setMentorId(mentor.getMentorId());
        profile.setFirstName(mentor.getFirstName());
        profile.setLastName(mentor.getLastName());
        profile.setEmail(mentor.getEmail());
        profile.setTitle(mentor.getTitle());
        profile.setProfession(mentor.getProfession());
        profile.setCompany(mentor.getCompany());
        profile.setExperienceYears(mentor.getExperienceYears());
        profile.setBio(mentor.getBio());
        profile.setProfileImageUrl(mentor.getProfileImageUrl());
        profile.setPositiveReviews(mentor.getPositiveReviews());
        profile.setTotalEnrollments(mentor.getTotalEnrollments());
        profile.setIsCertified(mentor.getIsCertified());
        profile.setStartYear(mentor.getStartYear());

        // Fetch subjects for this mentor and add enrollment count to each
        List<Subject> subjects = subjectRepository.findByMentor_Id(id);
        List<MentorProfileDTO.SubjectWithEnrollmentDTO> subjectDTOs = subjects.stream()
                .map(subject -> {
                    MentorProfileDTO.SubjectWithEnrollmentDTO dto = new MentorProfileDTO.SubjectWithEnrollmentDTO();
                    dto.setId(subject.getId());
                    dto.setSubjectName(subject.getSubjectName());
                    dto.setDescription(subject.getDescription());
                    dto.setCourseImageUrl(subject.getCourseImageUrl());
                    // Count how many sessions exist for this subject
                    dto.setEnrollmentCount(sessionRepository.countBySubject_Id(subject.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        profile.setSubjects(subjectDTOs);
        return sendOkResponse(profile);
    }

    // GET /api/v1/mentors/{id} — simple mentor by id (public)
    @GetMapping("{id}")
    public ResponseEntity<Mentor> getMentorById(@PathVariable Long id) {
        Mentor mentor = mentorService.getMentorById(id);
        return sendOkResponse(mentor);
    }

    // POST /api/v1/mentors — create mentor (admin or mentor role)
    @PostMapping
    @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "', '" + ROLE_MENTOR + "')")
    public ResponseEntity<Mentor> createMentor(
            @Valid @RequestBody MentorDTO mentorDTO,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Mentor mentor = modelMapper.map(mentorDTO, Mentor.class);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin || mentorDTO.getMentorId() == null) {
            mentor.setMentorId(userPrincipal.getId());
            mentor.setFirstName(userPrincipal.getFirstName());
            mentor.setLastName(userPrincipal.getLastName());
            mentor.setEmail(userPrincipal.getEmail());
        }

        Mentor createdMentor = mentorService.createNewMentor(mentor);
        return sendCreatedResponse(createdMentor);
    }

    // PUT /api/v1/mentors/{id} — update mentor (admin or mentor role)
    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "', '" + ROLE_MENTOR + "')")
    public ResponseEntity<Mentor> updateMentor(
            @PathVariable Long id,
            @Valid @RequestBody MentorDTO updatedMentorDTO) {
        Mentor mentor = modelMapper.map(updatedMentorDTO, Mentor.class);
        Mentor updatedMentor = mentorService.updateMentorById(id, mentor);
        return sendOkResponse(updatedMentor);
    }

    // DELETE /api/v1/mentors/{id} — delete mentor (admin only)
    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "')")
    public ResponseEntity<Mentor> deleteMentor(@PathVariable Long id) {
        mentorService.deleteMentor(id);
        return sendNoContentResponse();
    }
}