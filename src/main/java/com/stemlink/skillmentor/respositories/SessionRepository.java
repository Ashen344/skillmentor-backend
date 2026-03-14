package com.stemlink.skillmentor.respositories;

import com.stemlink.skillmentor.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByStudent_Email(String email);

    // Count how many sessions exist for a given subject (used for enrollment stats)
    long countBySubject_Id(Long subjectId);

    // Fetch all non-cancelled sessions for a student with a specific mentor
    // Overlap check is done in Java (avoids JPQL date arithmetic issues)
    @Query("""
        SELECT s FROM Session s
        WHERE s.student.email = :email
        AND s.mentor.id = :mentorId
        AND s.sessionStatus != 'cancelled'
    """)
    List<Session> findActiveSessionsForStudentAndMentor(
            @Param("email") String email,
            @Param("mentorId") Long mentorId
    );

    // Fetch all non-cancelled sessions for a student with a specific subject
    @Query("""
        SELECT s FROM Session s
        WHERE s.student.email = :email
        AND s.subject.id = :subjectId
        AND s.sessionStatus != 'cancelled'
    """)
    List<Session> findActiveSessionsForStudentAndSubject(
            @Param("email") String email,
            @Param("subjectId") Long subjectId
    );
}