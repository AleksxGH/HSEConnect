package org.example.hseconnect.model;

import java.util.ArrayList;
import java.util.List;

public class ProfileDto {
    private Long id;
    private Long userId;

    private String lastName;
    private String firstName;
    private String middleName;
    private String birthDate;
    private String avatarUrl;
    private String type;

    private String name;
    private String description;
    private String about;

    private int friendsCount;
    private int followersCount;

    private List<String> tags = new ArrayList<>();
    private List<String> interests = new ArrayList<>();

    private StudentInfo student;
    private EmployeeInfo employee;

    public ProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    public int getFriendsCount() { return friendsCount; }
    public void setFriendsCount(int friendsCount) { this.friendsCount = friendsCount; }
    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public StudentInfo getStudent() { return student; }
    public void setStudent(StudentInfo student) { this.student = student; }
    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public static class StudentInfo {
        private Long facultyId;
        private Long educationProgramId;
        private Long educationLevelId;
        private Long studyFormId;
        private Long academicStatusId;
        private Long campusId;

        private String campus;
        private String faculty;
        private String educationLevel;
        private String educationForm;
        private String program;
        private String graduationYear;
        private String course;
        private String status;

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
        public Long getCampusId() { return campusId; }
        public void setCampusId(Long campusId) { this.campusId = campusId; }
        public String getCampus() { return campus; }
        public void setCampus(String campus) { this.campus = campus; }
        public String getFaculty() { return faculty; }
        public void setFaculty(String faculty) { this.faculty = faculty; }
        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public String getEducationForm() { return educationForm; }
        public void setEducationForm(String educationForm) { this.educationForm = educationForm; }
        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }
        public String getGraduationYear() { return graduationYear; }
        public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class EmployeeInfo {
        private List<JobInfo> jobs = new ArrayList<>();
        private String address;
        private String workPhone;

        public List<JobInfo> getJobs() { return jobs; }
        public void setJobs(List<JobInfo> jobs) { this.jobs = jobs; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getWorkPhone() { return workPhone; }
        public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }
    }

    public static class JobInfo {
        private Long departmentId;
        private Long positionId;
        private String department;
        private String position;

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Long getPositionId() { return positionId; }
        public void setPositionId(Long positionId) { this.positionId = positionId; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
    }
}
