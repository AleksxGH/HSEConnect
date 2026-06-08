package org.example.hseconnect.services;

import org.example.hseconnect.model.ProfileDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProfileService {

    private final JdbcTemplate jdbcTemplate;

    public ProfileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProfileDto getProfileByUserId(Long userId) {
        validateUserId(userId);

        List<ProfileDto> result = jdbcTemplate.query("""
                SELECT profile_id, user_id, last_name, first_name, middle_name, birth_date, about, campus_id
                FROM app.profile
                WHERE user_id = ?
                """, (rs, rowNum) -> {
            ProfileDto dto = new ProfileDto();
            dto.setId(rs.getLong("user_id"));
            dto.setLastName(rs.getString("last_name"));
            dto.setFirstName(rs.getString("first_name"));
            dto.setMiddleName(rs.getString("middle_name"));

            Date birthDate = rs.getDate("birth_date");
            dto.setBirthDate(birthDate == null ? null : birthDate.toLocalDate().toString());

            dto.setAbout(rs.getString("about"));
            dto.setDescription(rs.getString("about") == null || rs.getString("about").isBlank()
                    ? "Пока нет описания"
                    : rs.getString("about"));
            dto.setName(buildFullName(dto));
            dto.setFriendsCount(countFriends(userId));
            dto.setFollowersCount(countFollowers(userId));
            dto.setInterests(loadInterests(userId));

            if (hasRole(userId, "student") || exists("student_profile", "user_id", userId)) {
                dto.setType("student");
                dto.setStudent(loadStudentInfo(userId));
            }

            if (hasRole(userId, "employee") || exists("employee_profile", "user_id", userId)) {
                dto.setType("employee");
                dto.setEmployee(loadEmployeeInfo(userId));
            }

            dto.setTags(buildTags(dto));
            return dto;
        }, userId);

        if (result.isEmpty()) {
            throw new RuntimeException("Профиль пользователя не найден");
        }

        return result.get(0);
    }

    @Transactional
    public ProfileDto saveQuestionnaire(Long userId, ProfileDto dto) {
        validateUserId(userId);
        validateQuestionnaire(dto);
        ensureUserExists(userId);

        Long campusId = null;
        if (dto.getStudent() != null && dto.getStudent().getCampus() != null && !dto.getStudent().getCampus().isBlank()) {
            campusId = findOrCreateCampus(dto.getStudent().getCampus());
        }

        upsertProfile(userId, dto, campusId);
        replaceRole(userId, dto.getType());

        if ("student".equals(dto.getType())) {
            saveStudentProfile(userId, dto.getStudent());
            deleteEmployeeProfile(userId);
        } else {
            saveEmployeeProfile(userId, dto.getEmployee());
            deleteStudentProfile(userId);
        }

        return getProfileByUserId(userId);
    }

    @Transactional
    public ProfileDto updateProfile(Long userId, ProfileDto profileDto) {
        return saveQuestionnaire(userId, profileDto);
    }

    private void upsertProfile(Long userId, ProfileDto dto, Long campusId) {
        int updated = jdbcTemplate.update("""
                UPDATE app.profile
                SET last_name = ?, first_name = ?, middle_name = ?, birth_date = ?, about = ?, campus_id = ?
                WHERE user_id = ?
                """,
                dto.getLastName().trim(),
                dto.getFirstName().trim(),
                blankToNull(dto.getMiddleName()),
                Date.valueOf(LocalDate.parse(dto.getBirthDate())),
                dto.getAbout(),
                campusId,
                userId
        );

        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO app.profile (user_id, last_name, first_name, middle_name, birth_date, avatar_url, about, campus_id)
                    VALUES (?, ?, ?, ?, ?, NULL, ?, ?)
                    """,
                    userId,
                    dto.getLastName().trim(),
                    dto.getFirstName().trim(),
                    blankToNull(dto.getMiddleName()),
                    Date.valueOf(LocalDate.parse(dto.getBirthDate())),
                    dto.getAbout(),
                    campusId
            );
        }
    }

    private void replaceRole(Long userId, String roleType) {
        jdbcTemplate.update("DELETE FROM app.user_role WHERE user_id = ?", userId);
        jdbcTemplate.update("INSERT INTO app.user_role (user_id, role_type) VALUES (?, ?)", userId, roleType);
    }

    private void saveStudentProfile(Long userId, ProfileDto.StudentInfo student) {
        if (student == null) throw new RuntimeException("Заполните данные студента");

        Long facultyId = findOrCreateSimple("faculty", "faculty_id", normalize(student.getFaculty(), "Не указано"));
        Long levelId = findOrCreateSimple("education_level", "education_level_id", normalize(student.getEducationForm(), "Не указано"));
        Long formId = findOrCreateSimple("study_form", "study_form_id", normalize(student.getEducationForm(), "Не указано"));
        Long statusId = findOrCreateSimple("academic_status", "academic_status_id", normalize(student.getStatus(), "active"));
        Long programId = findOrCreateProgram(normalize(student.getProgram(), "Не указано"), facultyId, levelId);
        Integer graduationYear = parseIntegerOrNull(student.getGraduationYear());

        int updated = jdbcTemplate.update("""
                UPDATE app.student_profile
                SET faculty_id = ?, education_program_id = ?, education_level_id = ?, study_form_id = ?,
                    academic_status_id = ?, graduation_year = ?
                WHERE user_id = ?
                """, facultyId, programId, levelId, formId, statusId, graduationYear, userId);

        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO app.student_profile
                    (user_id, faculty_id, education_program_id, education_level_id, study_form_id, academic_status_id, graduation_year)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, userId, facultyId, programId, levelId, formId, statusId, graduationYear);
        }
    }

    private void saveEmployeeProfile(Long userId, ProfileDto.EmployeeInfo employee) {
        if (employee == null) throw new RuntimeException("Заполните данные сотрудника");

        Long addressId = null;
        if (employee.getAddress() != null && !employee.getAddress().isBlank()) {
            addressId = createAddress(employee.getAddress());
        }

        Long employeeProfileId = getEmployeeProfileId(userId);

        if (employeeProfileId == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Long finalAddressId = addressId;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO app.employee_profile (user_id, work_phone, work_address_id)
                        VALUES (?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, userId);
                ps.setString(2, employee.getWorkPhone());
                if (finalAddressId == null) ps.setObject(3, null); else ps.setLong(3, finalAddressId);
                return ps;
            }, keyHolder);
            employeeProfileId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        } else {
            jdbcTemplate.update("""
                    UPDATE app.employee_profile
                    SET work_phone = ?, work_address_id = ?
                    WHERE employee_profile_id = ?
                    """, employee.getWorkPhone(), addressId, employeeProfileId);
        }

        jdbcTemplate.update("DELETE FROM app.employee_appointment WHERE employee_profile_id = ?", employeeProfileId);

        if (employee.getJobs() != null) {
            for (ProfileDto.JobInfo job : employee.getJobs()) {
                Long departmentId = findOrCreateSimple("department", "department_id", normalize(job.getDepartment(), "Не указано"));
                Long positionId = findOrCreateSimple("position", "position_id", normalize(job.getPosition(), "Не указано"));

                jdbcTemplate.update("""
                        INSERT INTO app.employee_appointment (employee_profile_id, department_id, position_id)
                        VALUES (?, ?, ?)
                        """, employeeProfileId, departmentId, positionId);
            }
        }
    }

    private ProfileDto.StudentInfo loadStudentInfo(Long userId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT c.name AS campus,
                           f.name AS faculty,
                           sf.name AS study_form,
                           ep.name AS program,
                           sp.graduation_year,
                           ast.name AS status
                    FROM app.student_profile sp
                    LEFT JOIN app.profile p ON p.user_id = sp.user_id
                    LEFT JOIN app.campus c ON c.campus_id = p.campus_id
                    LEFT JOIN app.faculty f ON f.faculty_id = sp.faculty_id
                    LEFT JOIN app.study_form sf ON sf.study_form_id = sp.study_form_id
                    LEFT JOIN app.education_program ep ON ep.education_program_id = sp.education_program_id
                    LEFT JOIN app.academic_status ast ON ast.academic_status_id = sp.academic_status_id
                    WHERE sp.user_id = ?
                    """, (rs, rowNum) -> {
                ProfileDto.StudentInfo student = new ProfileDto.StudentInfo();
                student.setCampus(rs.getString("campus"));
                student.setFaculty(rs.getString("faculty"));
                student.setEducationForm(rs.getString("study_form"));
                student.setProgram(rs.getString("program"));
                Integer graduationYear = rs.getObject("graduation_year", Integer.class);
                student.setGraduationYear(graduationYear == null ? null : graduationYear.toString());
                student.setCourse(calculateCourse(graduationYear));
                student.setStatus(rs.getString("status"));
                return student;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private ProfileDto.EmployeeInfo loadEmployeeInfo(Long userId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT employee_profile_id, work_phone, work_address_id
                    FROM app.employee_profile
                    WHERE user_id = ?
                    """, (rs, rowNum) -> {
                Long employeeProfileId = rs.getLong("employee_profile_id");

                ProfileDto.EmployeeInfo employee = new ProfileDto.EmployeeInfo();
                employee.setWorkPhone(rs.getString("work_phone"));
                employee.setAddress(loadAddress(rs.getObject("work_address_id", Long.class)));
                employee.setJobs(loadJobs(employeeProfileId));
                return employee;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<ProfileDto.JobInfo> loadJobs(Long employeeProfileId) {
        return jdbcTemplate.query("""
                SELECT d.name AS department, p.name AS position
                FROM app.employee_appointment ea
                LEFT JOIN app.department d ON d.department_id = ea.department_id
                LEFT JOIN app.position p ON p.position_id = ea.position_id
                WHERE ea.employee_profile_id = ?
                """, (rs, rowNum) -> {
            ProfileDto.JobInfo job = new ProfileDto.JobInfo();
            job.setDepartment(rs.getString("department"));
            job.setPosition(rs.getString("position"));
            return job;
        }, employeeProfileId);
    }

    private List<String> loadInterests(Long userId) {
        return jdbcTemplate.queryForList("""
                SELECT i.name
                FROM app.user_interest ui
                JOIN app.interest i ON i.interest_id = ui.interest_id
                WHERE ui.user_id = ?
                ORDER BY i.name
                """, String.class, userId);
    }

    private int countFriends(Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app.friendship
                WHERE user_id_1 = ? OR user_id_2 = ?
                """, Integer.class, userId, userId);
        return count == null ? 0 : count;
    }

    private int countFollowers(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app.follow WHERE following_id = ?", Integer.class, userId);
        return count == null ? 0 : count;
    }

    private boolean hasRole(Long userId, String roleType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app.user_role WHERE user_id = ? AND role_type = ?",
                Integer.class,
                userId,
                roleType
        );
        return count != null && count > 0;
    }

    private boolean exists(String tableName, String columnName, Long value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app." + tableName + " WHERE " + columnName + " = ?",
                Integer.class,
                value
        );
        return count != null && count > 0;
    }

    private void ensureUserExists(Long userId) {
        if (!exists("users", "user_id", userId)) {
            throw new RuntimeException("Пользователь не найден в базе данных");
        }
    }

    private Long findOrCreateSimple(String tableName, String idColumn, String name) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT " + idColumn + " FROM app." + tableName + " WHERE LOWER(name) = LOWER(?) LIMIT 1",
                Long.class,
                name
        );
        if (!ids.isEmpty()) return ids.get(0);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app." + tableName + " (name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Long findOrCreateCampus(String name) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT campus_id FROM app.campus WHERE LOWER(name) = LOWER(?) LIMIT 1",
                Long.class,
                name
        );
        if (!ids.isEmpty()) return ids.get(0);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app.campus (name, city) VALUES (?, '')",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Long findOrCreateProgram(String name, Long facultyId, Long levelId) {
        List<Long> ids = jdbcTemplate.queryForList("""
                SELECT education_program_id
                FROM app.education_program
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """, Long.class, name);
        if (!ids.isEmpty()) return ids.get(0);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO app.education_program (faculty_id, education_level_id, name, code)
                    VALUES (?, ?, ?, NULL)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, facultyId);
            ps.setLong(2, levelId);
            ps.setString(3, name);
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Long createAddress(String fullAddress) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO app.address (campus_id, city, street, building, room, full_address, latitude, longitude)
                    VALUES (NULL, '', NULL, NULL, NULL, ?, NULL, NULL)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fullAddress.trim());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private String loadAddress(Long addressId) {
        if (addressId == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT full_address FROM app.address WHERE address_id = ?",
                    String.class,
                    addressId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Long getEmployeeProfileId(Long userId) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT employee_profile_id FROM app.employee_profile WHERE user_id = ? LIMIT 1",
                Long.class,
                userId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void deleteStudentProfile(Long userId) {
        jdbcTemplate.update("DELETE FROM app.student_profile WHERE user_id = ?", userId);
    }

    private void deleteEmployeeProfile(Long userId) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT employee_profile_id FROM app.employee_profile WHERE user_id = ?",
                Long.class,
                userId
        );
        for (Long id : ids) {
            jdbcTemplate.update("DELETE FROM app.employee_appointment WHERE employee_profile_id = ?", id);
        }
        jdbcTemplate.update("DELETE FROM app.employee_profile WHERE user_id = ?", userId);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Пользователь не авторизован");
        }
    }

    private void validateQuestionnaire(ProfileDto dto) {
        if (dto == null) throw new RuntimeException("Анкета не заполнена");
        if (dto.getLastName() == null || dto.getLastName().isBlank()) throw new RuntimeException("Фамилия обязательна");
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) throw new RuntimeException("Имя обязательно");
        if (dto.getBirthDate() == null || dto.getBirthDate().isBlank()) throw new RuntimeException("Дата рождения обязательна");
        if (dto.getType() == null || dto.getType().isBlank()) throw new RuntimeException("Выберите роль");
        if (!dto.getType().equals("student") && !dto.getType().equals("employee")) {
            throw new RuntimeException("Некорректная роль пользователя");
        }
        LocalDate.parse(dto.getBirthDate());
    }

    private String buildFullName(ProfileDto dto) {
        List<String> parts = new ArrayList<>();
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) parts.add(dto.getLastName());
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) parts.add(dto.getFirstName());
        if (dto.getMiddleName() != null && !dto.getMiddleName().isBlank()) parts.add(dto.getMiddleName());
        return String.join(" ", parts);
    }

    private List<String> buildTags(ProfileDto dto) {
        List<String> tags = new ArrayList<>();

        if ("student".equals(dto.getType()) && dto.getStudent() != null) {
            tags.add("Студент");
            addIfPresent(tags, dto.getStudent().getFaculty());
            addIfPresent(tags, dto.getStudent().getProgram());
            addIfPresent(tags, dto.getStudent().getCourse());
            addIfPresent(tags, dto.getStudent().getStatus());
        }

        if ("employee".equals(dto.getType()) && dto.getEmployee() != null) {
            tags.add("Сотрудник");
            if (dto.getEmployee().getJobs() != null && !dto.getEmployee().getJobs().isEmpty()) {
                ProfileDto.JobInfo firstJob = dto.getEmployee().getJobs().get(0);
                addIfPresent(tags, firstJob.getDepartment());
                addIfPresent(tags, firstJob.getPosition());
            }
        }

        return tags;
    }

    private void addIfPresent(List<String> values, String value) {
        if (value != null && !value.isBlank()) values.add(value);
    }

    private String calculateCourse(Integer graduationYear) {
        if (graduationYear == null) return null;
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int academicYear = currentMonth >= Month.SEPTEMBER.getValue() ? currentYear : currentYear - 1;
        int course = graduationYear - academicYear;
        if (course <= 0) return "Выпускник";
        return course + " курс";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value.trim());
    }
}
