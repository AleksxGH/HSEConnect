package org.example.hseconnect.model;

import java.util.List;

public class ProfileDto {
    private Long id;

    private String lastName;
    private String firstName;
    private String middleName;
    private String birthDate;
    private String type;

    private String name;
    private String description;
    private String about;

    private int friendsCount;
    private int followersCount;

    private List<String> tags;
    private List<String> interests;

    private StudentInfo student;
    private EmployeeInfo employee;

    public ProfileDto() {}

    public Long getId() { return id; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getBirthDate() { return birthDate; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAbout() { return about; }
    public int getFriendsCount() { return friendsCount; }
    public int getFollowersCount() { return followersCount; }
    public List<String> getTags() { return tags; }
    public List<String> getInterests() { return interests; }
    public StudentInfo getStudent() { return student; }
    public EmployeeInfo getEmployee() { return employee; }

    public void setId(Long id) { this.id = id; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setType(String type) { this.type = type; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAbout(String about) { this.about = about; }
    public void setFriendsCount(int friendsCount) { this.friendsCount = friendsCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public void setStudent(StudentInfo student) { this.student = student; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public static class StudentInfo {
        private String campus;
        private String faculty;
        private String educationForm;
        private String program;
        private String graduationYear;
        private String course;
        private String status;

        public String getCampus() { return campus; }
        public String getFaculty() { return faculty; }
        public String getEducationForm() { return educationForm; }
        public String getProgram() { return program; }
        public String getGraduationYear() { return graduationYear; }
        public String getCourse() { return course; }
        public String getStatus() { return status; }

        public void setCampus(String campus) { this.campus = campus; }
        public void setFaculty(String faculty) { this.faculty = faculty; }
        public void setEducationForm(String educationForm) { this.educationForm = educationForm; }
        public void setProgram(String program) { this.program = program; }
        public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
        public void setCourse(String course) { this.course = course; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class EmployeeInfo {
        private List<JobInfo> jobs;
        private String address;
        private String workPhone;

        public List<JobInfo> getJobs() { return jobs; }
        public String getAddress() { return address; }
        public String getWorkPhone() { return workPhone; }

        public void setJobs(List<JobInfo> jobs) { this.jobs = jobs; }
        public void setAddress(String address) { this.address = address; }
        public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }
    }

    public static class JobInfo {
        private String department;
        private String position;

        public String getDepartment() { return department; }
        public String getPosition() { return position; }

        public void setDepartment(String department) { this.department = department; }
        public void setPosition(String position) { this.position = position; }
    }
}