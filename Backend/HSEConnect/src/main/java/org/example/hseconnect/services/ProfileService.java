package org.example.hseconnect.services;

import org.example.hseconnect.model.ProfileDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProfileService {

    private static final Map<Long, ProfileDto> profilesByUserId = new HashMap<>();

    public ProfileDto getProfileByUserId(Long userId) {
        validateUserId(userId);

        ProfileDto profile = profilesByUserId.get(userId);

        if (profile == null) {
            throw new RuntimeException("Профиль пользователя не найден");
        }

        return profile;
    }

    public ProfileDto saveQuestionnaire(Long userId, ProfileDto dto) {
        validateUserId(userId);
        validateQuestionnaire(dto);

        ProfileDto saved = new ProfileDto();

        saved.setId(userId);

        saved.setLastName(dto.getLastName());
        saved.setFirstName(dto.getFirstName());
        saved.setMiddleName(dto.getMiddleName());
        saved.setBirthDate(dto.getBirthDate());
        saved.setType(dto.getType());

        saved.setName(buildFullName(dto));

        saved.setAbout(dto.getAbout());
        saved.setDescription(
                dto.getAbout() == null || dto.getAbout().isBlank()
                        ? "Пока нет описания"
                        : dto.getAbout()
        );

        saved.setFriendsCount(0);
        saved.setFollowersCount(0);

        saved.setInterests(
                dto.getInterests() == null
                        ? new ArrayList<>()
                        : dto.getInterests()
        );

        if ("student".equals(dto.getType())) {
            saved.setStudent(dto.getStudent());
            saved.setEmployee(null);
        }

        if ("employee".equals(dto.getType())) {
            saved.setEmployee(dto.getEmployee());
            saved.setStudent(null);
        }

        saved.setTags(buildTags(saved));

        profilesByUserId.put(userId, saved);

        return saved;
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Пользователь не авторизован");
        }
    }

    private void validateQuestionnaire(ProfileDto dto) {
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new RuntimeException("Фамилия обязательна");
        }

        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            throw new RuntimeException("Имя обязательно");
        }

        if (dto.getBirthDate() == null || dto.getBirthDate().isBlank()) {
            throw new RuntimeException("Дата рождения обязательна");
        }

        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new RuntimeException("Выберите роль");
        }

        if (!dto.getType().equals("student") && !dto.getType().equals("employee")) {
            throw new RuntimeException("Некорректная роль пользователя");
        }
    }

    private String buildFullName(ProfileDto dto) {
        List<String> parts = new ArrayList<>();

        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            parts.add(dto.getLastName());
        }

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            parts.add(dto.getFirstName());
        }

        if (dto.getMiddleName() != null && !dto.getMiddleName().isBlank()) {
            parts.add(dto.getMiddleName());
        }

        return String.join(" ", parts);
    }

    private List<String> buildTags(ProfileDto dto) {
        List<String> tags = new ArrayList<>();

        if ("student".equals(dto.getType()) && dto.getStudent() != null) {
            tags.add("Студент");

            if (dto.getStudent().getFaculty() != null && !dto.getStudent().getFaculty().isBlank()) {
                tags.add(dto.getStudent().getFaculty());
            }

            if (dto.getStudent().getProgram() != null && !dto.getStudent().getProgram().isBlank()) {
                tags.add(dto.getStudent().getProgram());
            }

            if (dto.getStudent().getCourse() != null && !dto.getStudent().getCourse().isBlank()) {
                tags.add(dto.getStudent().getCourse());
            }

            if (dto.getStudent().getStatus() != null && !dto.getStudent().getStatus().isBlank()) {
                tags.add(dto.getStudent().getStatus());
            }
        }

        if ("employee".equals(dto.getType()) && dto.getEmployee() != null) {
            tags.add("Сотрудник");

            if (dto.getEmployee().getJobs() != null && !dto.getEmployee().getJobs().isEmpty()) {
                ProfileDto.JobInfo firstJob = dto.getEmployee().getJobs().get(0);

                if (firstJob.getDepartment() != null && !firstJob.getDepartment().isBlank()) {
                    tags.add(firstJob.getDepartment());
                }

                if (firstJob.getPosition() != null && !firstJob.getPosition().isBlank()) {
                    tags.add(firstJob.getPosition());
                }
            }
        }

        return tags;
    }

    public ProfileDto updateProfile(Long userId, ProfileDto profileDto) {
        profilesByUserId.put(userId, profileDto);
        return profileDto;
    }
}
