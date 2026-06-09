package org.example.hseconnect.model;

import java.util.ArrayList;
import java.util.List;

public class ProfileDto {
    private Long id;

    private String name;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthDate;

    private String about;
    private String description;
    private String avatarUrl;

    private String type;

    private Integer friendsCount;
    private Integer followersCount;

    private List<String> interests = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    private StudentInfo student;
    private EmployeeInfo employee;

    public static class StudentInfo {
        private String campus;
        private String faculty;
        private String educationProgram;
        private String program;

        private String educationLevel;
        private String studyForm;

        private String academicStatus;
        private String status;

        private String graduationYear;
        private String course;

        public String getCampus() {
            return campus;
        }

        public void setCampus(String campus) {
            this.campus = campus;
        }

        public String getFaculty() {
            return faculty;
        }

        public void setFaculty(String faculty) {
            this.faculty = faculty;
        }

        public String getEducationProgram() {
            return educationProgram;
        }

        public void setEducationProgram(String educationProgram) {
            this.educationProgram = educationProgram;
        }

        public String getProgram() {
            return program;
        }

        public void setProgram(String program) {
            this.program = program;
        }

        public String getEducationLevel() {
            return educationLevel;
        }

        public void setEducationLevel(String educationLevel) {
            this.educationLevel = educationLevel;
        }

        public String getStudyForm() {
            return studyForm;
        }

        public void setStudyForm(String studyForm) {
            this.studyForm = studyForm;
        }

        public String getAcademicStatus() {
            return academicStatus;
        }

        public void setAcademicStatus(String academicStatus) {
            this.academicStatus = academicStatus;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getGraduationYear() {
            return graduationYear;
        }

        public void setGraduationYear(String graduationYear) {
            this.graduationYear = graduationYear;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }
    }

    public static class EmployeeInfo {
        private String workPhone;
        private String address;
        private List<JobInfo> jobs = new ArrayList<>();

        public String getWorkPhone() {
            return workPhone;
        }

        public void setWorkPhone(String workPhone) {
            this.workPhone = workPhone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public List<JobInfo> getJobs() {
            return jobs;
        }

        public void setJobs(List<JobInfo> jobs) {
            this.jobs = jobs;
        }
    }

    public static class JobInfo {
        private String department;
        private String position;

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(Integer friendsCount) {
        this.friendsCount = friendsCount;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public void setStudent(StudentInfo student) {
        this.student = student;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }
}