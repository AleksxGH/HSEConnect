package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "education_program")
public class EducationProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "education_program_id")
    private Long id;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Column(name = "education_level_id")
    private Long educationLevelId;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    public EducationProgram() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public Long getEducationLevelId() { return educationLevelId; }
    public void setEducationLevelId(Long educationLevelId) { this.educationLevelId = educationLevelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

}
