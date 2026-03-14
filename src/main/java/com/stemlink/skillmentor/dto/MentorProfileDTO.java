package com.stemlink.skillmentor.dto.response;

import lombok.Data;

import java.util.List;

// Full mentor profile returned to the public profile page.
// Includes subjects with their enrollment counts.
@Data
public class MentorProfileDTO {
    private Long id;
    private String mentorId;
    private String firstName;
    private String lastName;
    private String email;
    private String title;
    private String profession;
    private String company;
    private int experienceYears;
    private String bio;
    private String profileImageUrl;
    private Integer positiveReviews;
    private Integer totalEnrollments;
    private Boolean isCertified;
    private String startYear;
    private List<SubjectWithEnrollmentDTO> subjects;

    // Nested DTO — one subject card on the profile page
    @Data
    public static class SubjectWithEnrollmentDTO {
        private Long id;
        private String subjectName;
        private String description;
        private String courseImageUrl;
        private long enrollmentCount; // how many sessions exist for this subject
    }
}