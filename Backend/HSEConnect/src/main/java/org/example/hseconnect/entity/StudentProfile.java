package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "student_profile")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_profile_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Column(name = "education_program_id")
    private Long educationProgramId;

    @Column(name = "education_level_id")
    private Long educationLevelId;

    @Column(name = "study_form_id")
    private Long studyFormId;

    @Column(name = "academic_status_id")
    private Long academicStatusId;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    public StudentProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public Long getEducationProgramId() { return educationProgramId; }
    public void setEducationProgramId(Long educationProgramId) { this.educationProgramId = educationProgramId; }

    public Long getEducationLevelId() { return educationLevelId; }
    public void setEducationLevelId(Long educationLevelId) { this.educationLevelId = educationLevelId; }

    public Long getStudyFormId() { return studyFormId; }
    public void setStudyFormId(Long studyFormId) { this.studyFormId = studyFormId; }

    public Long getAcademicStatusId() { return academicStatusId; }
    public void setAcademicStatusId(Long academicStatusId) { this.academicStatusId = academicStatusId; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

}
